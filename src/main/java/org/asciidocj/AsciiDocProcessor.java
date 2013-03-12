/*
 * Copyright (c) 2013 Ricardo Arguello
 *
 * Based on pegdown (c) 2010-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidocj;

import org.asciidocj.ast.DocumentNode;
import org.parboiled.Parboiled;

/**
 * An AsciiDoc-to-HTML processor based on a PEG parser implemented with
 * parboiled.
 * 
 * An AsciiDocProcessor is not thread-safe (since it internally reused the
 * parboiled parser instance). If you need to process AsciiDoc source in
 * parallel create one AsciiDocProcessor per thread!
 * 
 * @see <a href="http://www.asciidoc.org/">AsciiDoc</a>
 * @see <a href="http://www.parboiled.org/">parboiled.org</a>
 */
public class AsciiDocProcessor {

	public static final long DEFAULT_MAX_PARSING_TIME = 2000;

	public final AsciiDocParser parser;

	/**
	 * Creates a new processor instance with the default parsing timeout.
	 */
	public AsciiDocProcessor() {
		this(DEFAULT_MAX_PARSING_TIME);
	}

	/**
	 * Creates a new processor instance with the given parsing timeout.
	 * 
	 * @param maxParsingTimeInMillis
	 */
	public AsciiDocProcessor(long maxParsingTimeInMillis) {
		this(Parboiled.createParser(AsciiDocParser.class,
				maxParsingTimeInMillis,
				new DefaultParseRunnerProvider()));
	}

	/**
	 * Creates a new processor instance using the given Parser.
	 * 
	 * @param parser
	 *            the parser instance to use
	 */
	public AsciiDocProcessor(AsciiDocParser parser) {
		this.parser = parser;
	}

	/**
	 * Converts the given asciidoc source to HTML. If the input cannot be parsed
	 * within the configured parsing timeout the method returns null.
	 * 
	 * @param asciidocSource
	 *            the asciidoc source to convert
	 * @return the HTML
	 */
	public String asciidocToHtml(String asciidocSource) {
		return asciidocToHtml(asciidocSource.toCharArray());
	}

	/**
	 * Converts the given asciidoc source to HTML. If the input cannot be parsed
	 * within the configured parsing timeout the method returns null.
	 * 
	 * @param asciidocSource
	 *            the asciidoc source to convert
	 * @return the HTML
	 */
	public String asciidocToHtml(char[] asciidocSource) {
		try {
			DocumentNode astRoot = parseAsciidoc(asciidocSource);
			return new ToHtmlSerializer().toHtml(astRoot);
		} catch (ParsingTimeoutException e) {
			return null;
		}
	}

	/**
	 * Parses the given asciidoc source and returns the root node of the
	 * generated Abstract Syntax Tree. If the input cannot be parsed within the
	 * configured parsing timeout the method throws a ParsingTimeoutException.
	 * 
	 * @param asciidocSource
	 *            the asciidoc source to convert
	 * @return the AST root
	 */
	public DocumentNode parseAsciidoc(char[] asciidocSource) {
		return parser.parse(prepareSource(asciidocSource));
	}

	/**
	 * Adds two trailing newlines.
	 * 
	 * @param source
	 *            the asciidoc source to process
	 * @return the processed source
	 */
	private char[] prepareSource(char[] source) {
		char[] src = new char[source.length + 2];
		System.arraycopy(source, 0, src, 0, source.length);
		src[source.length] = '\n';
		src[source.length + 1] = '\n';
		return src;
	}
}