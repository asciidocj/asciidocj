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

import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;

import org.junit.Test;

public class HeadersTest {
	
	@Test
	public void documentTitleWithMultiLineSyntax() throws Exception {
		String title1 = "My Title";
		String title2 = "========";

		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml(title1 + "\n" + title2);
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);

		html = processor.asciidocToHtml(title1 + "\n" + title2 + "\n");
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);
	}

	@Test
	public void documentTitleWithMultiLineSyntaxGiveOneChar() throws Exception {
		String title1 = "My Title";
		String title2 = "=========";

		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml(title1 + "\n" + title2);
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);

		html = processor.asciidocToHtml(title1 + "\n" + title2 + "\n");
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);
	}

	@Test
	public void documentTitleWithMultiLineSyntaxGiveTwoChars() throws Exception {
		String title1 = "My Title";
		String title2 = "==========";

		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml(title1 + "\n" + title2);
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);

		html = processor.asciidocToHtml(title1 + "\n" + title2 + "\n");
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);
	}

	@Test
	public void documentTitleWithMultiLineSyntaxGiveThreeChars()
			throws Exception {
		String title1 = "My Title";
		String title2 = "===========";

		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml(title1 + "\n" + title2);
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);

		html = processor.asciidocToHtml(title1 + "\n" + title2 + "\n");
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);
	}

	@Test
	public void documentTitleWithMultiLineSyntaxTakeOneChar() throws Exception {
		String title1 = "My Title";
		String title2 = "=======";

		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml(title1 + "\n" + title2);
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);

		html = processor.asciidocToHtml(title1 + "\n" + title2 + "\n");
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);
	}

	@Test
	public void documentTitleWithMultiLineSyntaxTakeTwoChars() throws Exception {
		String title1 = "My Title";
		String title2 = "======";

		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml(title1 + "\n" + title2);
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);

		html = processor.asciidocToHtml(title1 + "\n" + title2 + "\n");
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);
	}

	//@Test
	public void notEnoughCharsForAMultiLineDocumentTitle() throws Exception {
		String title1 = "My Title";
		String title2 = "=====";

		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml(title1 + "\n" + title2);
		assertXpathNotExists("//h1", html);

		html = processor.asciidocToHtml(title1 + "\n" + title2 + "\n");
		assertXpathNotExists("//h1", html);
	}

	//@Test
	public void tooManyCharsForAMultiLineDocumentTitle() throws Exception {
		String title1 = "My Title";
		String title2 = "===========";

		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml(title1 + "\n" + title2);
		assertXpathNotExists("//h1", html);

		html = processor.asciidocToHtml(title1 + "\n" + title2 + "\n");
		assertXpathNotExists("//h1", html);
	}

	//@Test
	public void documentTitleWithMultiLineSyntaxCannotBeginWithADot()
			throws Exception {
		String title1 = ".My Title";
		String title2 = "=========";

		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml(title1 + "\n" + title2);
		assertXpathNotExists("//h1", html);

		html = processor.asciidocToHtml(title1 + "\n" + title2 + "\n");
		assertXpathNotExists("//h1", html);
	}

	@Test
	public void documentTitleWithSingleLineSyntax() throws Exception {
		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml("= My Title");
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);
	}

	@Test
	public void documentTitleWithSymmetricSyntax() throws Exception {
		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml("= My Title =");
		assertXpathExists("//h1[not(@id)][text() = 'My Title']", html);
	}

	@Test
	public void level1WithSingleLineSyntax() throws Exception {
		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml("== My Title");
		assertXpathExists("//h2[not(@id)][text() = 'My Title']", html);
	}

	@Test
	public void level1WithSingleLineSyntaxWithTrailingSpace() throws Exception {
		AsciiDocProcessor processor = new AsciiDocProcessor();
		String html = processor.asciidocToHtml("== My Title ");
		assertXpathExists("//h2[not(@id)][text() = 'My Title']", html);
	}
}