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

import static org.parboiled.common.Preconditions.checkArgNotNull;

import org.asciidocj.ast.DocumentNode;
import org.asciidocj.ast.Node;
import org.asciidocj.ast.ParaNode;
import org.asciidocj.ast.SimpleNode;
import org.asciidocj.ast.SuperNode;
import org.asciidocj.ast.TextNode;
import org.asciidocj.ast.TitleNode;
import org.asciidocj.ast.Visitor;

public class ToHtmlSerializer implements Visitor {

	protected Printer printer = new Printer();

	public ToHtmlSerializer() {
	}

	public String toHtml(DocumentNode astRoot) {
		checkArgNotNull(astRoot, "astRoot");
		astRoot.accept(this);
		return printer.getString();
	}

	public void visit(DocumentNode node) {
		visitChildren(node);
	}

	public void visit(TitleNode node) {
		printTag(node, "h" + node.getLevel());
	}

	public void visit(ParaNode node) {
		printTag(node, "p");
	}

	public void visit(SimpleNode node) {
		switch (node.getType()) {
		case Apostrophe:
			printer.print("&rsquo;");
			break;
		case Ellipsis:
			printer.print("&hellip;");
			break;
		case Emdash:
			printer.print("&mdash;");
			break;
		case Endash:
			printer.print("&ndash;");
			break;
		case HRule:
			printer.println().print("<hr/>");
			break;
		case Linebreak:
			printer.print("<br/>");
			break;
		case Nbsp:
			printer.print("&nbsp;");
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public void visit(TextNode node) {
		printer.print(node.getText());
	}

	public void visit(SuperNode node) {
		visitChildren(node);
	}

	public void visit(Node node) {
		// override this method for processing custom Node implementations
		throw new RuntimeException("Not implemented");
	}

	// helpers

	protected void visitChildren(SuperNode node) {
		for (Node child : node.getChildren()) {
			child.accept(this);
		}
	}

	protected void printTag(TextNode node, String tag) {
		printer.print('<').print(tag).print('>');
		printer.printEncoded(node.getText());
		printer.print('<').print('/').print(tag).print('>');
	}

	protected void printTag(SuperNode node, String tag) {
		printer.print('<').print(tag).print('>');
		visitChildren(node);
		printer.print('<').print('/').print(tag).print('>');
	}

	protected void printIndentedTag(SuperNode node, String tag) {
		printer.println().print('<').print(tag).print('>').indent(+2);
		visitChildren(node);
		printer.indent(-2).println().print('<').print('/').print(tag)
				.print('>');
	}

	protected void printImageTag(SuperNode imageNode, String url) {
		printer.print("<img src=\"").print(url).print("\"  alt=\"")
				.printEncoded(printChildrenToString(imageNode)).print("\"/>");
	}

	private void printAttribute(String name, String value) {
		printer.print(' ').print(name).print('=').print('"').print(value)
				.print('"');
	}

	protected String printChildrenToString(SuperNode node) {
		Printer priorPrinter = printer;
		printer = new Printer();
		visitChildren(node);
		String result = printer.getString();
		printer = priorPrinter;
		return result;
	}

	protected String normalize(String string) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			switch (c) {
			case ' ':
			case '\n':
			case '\t':
				continue;
			}
			sb.append(Character.toLowerCase(c));
		}
		return sb.toString();
	}
}