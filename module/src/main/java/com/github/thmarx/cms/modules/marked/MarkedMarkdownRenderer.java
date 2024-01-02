package com.github.thmarx.cms.modules.marked;

/*-
 * #%L
 * cms-server
 * %%
 * Copyright (C) 2023 Marx-Software
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.github.thmarx.cms.api.markdown.MarkdownRenderer;
import com.github.thmarx.cms.api.request.ThreadLocalRequestContext;
import com.github.thmarx.cms.api.request.features.IsPreviewFeature;
import com.github.thmarx.cms.api.request.features.SitePropertiesFeature;
import io.github.gitbucket.markedj.Marked;
import io.github.gitbucket.markedj.Options;
import io.github.gitbucket.markedj.Renderer;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

/**
 *
 * @author t.marx
 */
@Slf4j
public class MarkedMarkdownRenderer implements MarkdownRenderer {


	public MarkedMarkdownRenderer() {
	}

	@Override
	public void close() {
	}

	@Override
	public String excerpt(String markdown, int length) {
		String content = render(markdown);
		String text = Jsoup.parse(content).text();

		if (text.length() <= length) {
			return text;
		} else {
			return text.substring(0, length);
		}
	}

	@Override
	public String render(String markdown) {
		final Options options = new Options();
		options.getSafelist().removeProtocols("img", "src", "http", "https");
		options.getSafelist().removeProtocols("a", "href", "ftp", "http", "https", "mailto", "#");
		
		return Marked.marked(markdown, options, new Renderer(options) {
			@Override
			public String link(String href, String title, String text) {
				
				var requestContext = ThreadLocalRequestContext.REQUEST_CONTEXT.get();
				
				if (requestContext != null
						&& !href.startsWith("http") && !href.startsWith("https")) {
					
					if (requestContext.has(SitePropertiesFeature.class)) {
						var contextPath = requestContext.get(SitePropertiesFeature.class).siteProperties().contextPath();
						if (!"/".equals(contextPath) && !href.startsWith(contextPath) && href.startsWith("/")) {
							href = contextPath + href;
						}
					}
					if (requestContext.has(IsPreviewFeature.class)) {
						if (href.contains("?")) {
							href += "&preview";
						} else {
							href += "?preview";
						}
					}
				}
				
				return super.link(href, title, text);
			}
			
		});
	}

}
