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

import static org.parboiled.common.StringUtils.repeat;
import static org.parboiled.errors.ErrorUtils.printParseErrors;

import java.util.List;

import org.asciidocj.ast.AbstractNode;
import org.asciidocj.ast.DocumentNode;
import org.asciidocj.ast.Node;
import org.asciidocj.ast.ParaNode;
import org.asciidocj.ast.SimpleNode;
import org.asciidocj.ast.SimpleNode.Type;
import org.asciidocj.ast.SuperNode;
import org.asciidocj.ast.TextNode;
import org.asciidocj.ast.TitleNode;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.MemoMismatches;
import org.parboiled.common.ArrayBuilder;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.StringBuilderVar;

/**
 * Parboiled parser for the standard AsciiDoc syntax. Builds an Abstract Syntax
 * Tree (AST) of {@link Node} objects.
 */
public class AsciiDocParser extends BaseParser<Object> {

	public static ParseRunnerProvider defaultParseRunnerProvider = new DefaultParseRunnerProvider();

	protected final long maxParsingTimeInMillis;
	protected final ParseRunnerProvider parseRunnerProvider;

	long parsingStartTimeStamp = 0L;

	public AsciiDocParser(Long maxParsingTimeInMillis,
			ParseRunnerProvider parseRunnerProvider) {
		this.maxParsingTimeInMillis = maxParsingTimeInMillis;
		this.parseRunnerProvider = parseRunnerProvider;
	}

	public DocumentNode parse(char[] source) {
		DocumentNode root = parseInternal(source);
		return root;
	}

	// *********** DOCUMENT ***********

	public Rule Document() {
		return NodeSequence(push(new DocumentNode()),
				ZeroOrMore(Section(), addAsChild()));
	}

	public Rule Section() {
		return NodeSequence(Title(), Optional(SectionBody()));
	}

	public Rule Title() {
		return NodeSequence(FirstOf(OneLineTitle(), TwoLineTitle()));
	}

	public Rule SectionBody() {
		return NodeSequence(OneOrMore(Block(), addAsChild()));
	}

	// ************* BLOCKS ****************

	public Rule Block() {
		return Sequence(ZeroOrMore(BlankLine()),
				FirstOf(new ArrayBuilder<Rule>().add(Para(), Inlines()).get()));
	}

	public Rule Para() {
		return NodeSequence(NonindentSpace(), Inlines(), push(new ParaNode(
				popAsNode())), OneOrMore(BlankLine()));
	}

	// ************* HEADINGS ****************

	/*
	 * public Rule Header() { return NodeSequence(DocumentTitle()); }
	 */
	/*
	 * public Rule DocumentTitle() { return Sequence(OneLineTitleStart(),
	 * Optional(Sp()), OneOrMore(OneLineTitleInline(), addAsChild()),
	 * Optional(Sp(), ZeroOrMore('='), Sp()), Newline()); }
	 */
	public Rule OneLineTitle() {
		return Sequence(OneLineTitleStart(), Optional(Sp()),
				OneOrMore(OneLineTitleInline(), addAsChild()),
				Optional(Sp(), ZeroOrMore('='), Sp()), Newline());
	}

	public Rule OneLineTitleStart() {
		return Sequence(FirstOf("=====", "====", "===", "==", "="),
				push(new TitleNode(match().length())));
	}

	public Rule OneLineTitleInline() {
		return Sequence(TestNot(Newline()),
				TestNot(Optional(Sp()), ZeroOrMore('='), Sp(), Newline()),
				Inline());
	}

	public Rule TwoLineTitle() {
		return Sequence(
				Test(OneOrMore(NotNewline(), ANY),
						Newline(),
						FirstOf(NOrMore('=', 3), NOrMore('-', 3),
								NOrMore('~', 3), NOrMore('^', 3),
								NOrMore('+', 3)), Newline()),
				FirstOf(TwoLineTitleLevel1(), TwoLineTitleLevel2()));
	}

	public Rule TwoLineTitleLevel1() {
		return Sequence(TwoLineTitleInline(),
				push(new TitleNode(1, popAsNode())),
				ZeroOrMore(TwoLineTitleInline(), addAsChild()), Newline(),
				NOrMore('=', 3), Newline());
	}

	public Rule TwoLineTitleLevel2() {
		return Sequence(TwoLineTitleInline(),
				push(new TitleNode(2, popAsNode())),
				ZeroOrMore(TwoLineTitleInline(), addAsChild()), Newline(),
				NOrMore('-', 3), Newline());
	}

	public Rule TwoLineTitleInline() {
		return Sequence(TestNot(Endline()), Inline());
	}

	public Rule Inlines() {
		return NodeSequence(InlineOrIntermediateEndline(), push(new SuperNode(
				popAsNode())),
				ZeroOrMore(InlineOrIntermediateEndline(), addAsChild()),
				Optional(Endline(), drop()));
	}

	public Rule InlineOrIntermediateEndline() {
		return FirstOf(Sequence(TestNot(Endline()), Inline()),
				Sequence(Endline(), Test(Inline())));
	}

	@MemoMismatches
	public Rule Inline() {
		return Sequence(checkForParsingTimeout(), NonLinkInline());
	}

	// TODO FIX
	public Rule NonLinkInline() {
		return FirstOf(new ArrayBuilder<Rule>().add(Str(), Endline(), Space())
				.get());
	}

	@MemoMismatches
	public Rule Endline() {
		return NodeSequence(FirstOf(LineBreak(), TerminalEndline(),
				NormalEndline()));
	}

	public Rule LineBreak() {
		return Sequence("  ", NormalEndline(), poke(new SimpleNode(
				Type.Linebreak)));
	}

	public Rule TerminalEndline() {
		return NodeSequence(Sp(), Newline(), Test(EOI),
				push(new TextNode("\n")));
	}

	public Rule NormalEndline() {
		return Sequence(
				Sp(),
				Newline(),
				TestNot(FirstOf(
						BlankLine(),
						'>',
						OneLineTitleStart(),
						Sequence(ZeroOrMore(NotNewline(), ANY), Newline(),
								FirstOf(NOrMore('=', 3), NOrMore('-', 3)),
								Newline()))));
	}

	// ************* LINES ****************

	public Rule BlankLine() {
		return Sequence(Sp(), Newline());
	}

	public Rule Line(StringBuilderVar sb) {
		return Sequence(Sequence(ZeroOrMore(NotNewline(), ANY), Newline()),
				sb.append(match()));
	}

	// ************* BASICS ****************

	public Rule Str() {
		return NodeSequence(OneOrMore(NormalChar()),
				push(new TextNode(match())));
	}

	public Rule Space() {
		return NodeSequence(OneOrMore(Spacechar()), push(new TextNode(" ")));
	}

	public Rule Spn1() {
		return Sequence(Sp(), Optional(Newline(), Sp()));
	}

	public Rule Sp() {
		return ZeroOrMore(Spacechar());
	}

	public Rule Spacechar() {
		return AnyOf(" \t");
	}

	public Rule Nonspacechar() {
		return Sequence(TestNot(Spacechar()), NotNewline(), ANY);
	}

	@MemoMismatches
	public Rule NormalChar() {
		return Sequence(TestNot(Spacechar()), NotNewline(), ANY);
	}

	public Rule NotNewline() {
		return TestNot(AnyOf("\n\r"));
	}

	public Rule Newline() {
		return FirstOf('\n', Sequence('\r', Optional('\n')));
	}

	public Rule NonindentSpace() {
		return FirstOf("   ", "  ", " ", EMPTY);
	}

	public Rule Indent() {
		return FirstOf('\t', "    ");
	}

	public Rule Alphanumeric() {
		return FirstOf(Letter(), Digit());
	}

	public Rule Letter() {
		return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
	}

	public Rule Digit() {
		return CharRange('0', '9');
	}

	// ************* HELPERS ****************

	public Rule NOrMore(char c, int n) {
		return Sequence(repeat(c, n), ZeroOrMore(c));
	}

	public Rule NodeSequence(Object... nodeRules) {
		return Sequence(push(getContext().getCurrentIndex()),
				Sequence(nodeRules), setIndices());
	}

	public boolean setIndices() {
		AbstractNode node = (AbstractNode) peek();
		node.setStartIndex((Integer) pop(1));
		node.setEndIndex(currentIndex());
		return true;
	}

	public boolean addAsChild() {
		SuperNode parent = (SuperNode) peek(1);
		List<Node> children = parent.getChildren();
		Node child = popAsNode();
		if (child.getClass() == TextNode.class && !children.isEmpty()) {
			Node lastChild = children.get(children.size() - 1);
			if (lastChild.getClass() == TextNode.class) {
				// collapse peer TextNodes
				TextNode last = (TextNode) lastChild;
				TextNode current = (TextNode) child;
				last.append(current.getText());
				last.setEndIndex(current.getEndIndex());
				return true;
			}
		}
		children.add(child);
		return true;
	}

	public Node popAsNode() {
		return (Node) pop();
	}

	public String popAsString() {
		return (String) pop();
	}

	// called for inner parses for list items and blockquotes
	public DocumentNode parseInternal(StringBuilderVar block) {
		char[] chars = block.getChars();
		int[] ixMap = new int[chars.length + 1]; // map of cleaned indices to
													// original indices

		// strip out CROSSED_OUT characters and build index map
		StringBuilder clean = new StringBuilder();
		// for (int i = 0; i < chars.length; i++) {
		// char c = chars[i];
		// if (c != CROSSED_OUT) {
		// ixMap[clean.length()] = i;
		// clean.append(c);
		// }
		// }
		ixMap[clean.length()] = chars.length;

		// run inner parse
		char[] cleaned = new char[clean.length()];
		clean.getChars(0, cleaned.length, cleaned, 0);
		DocumentNode rootNode = parseInternal(cleaned);

		// correct AST indices with index map
		fixIndices(rootNode, ixMap);

		return rootNode;
	}

	protected void fixIndices(Node node, int[] ixMap) {
		((AbstractNode) node).mapIndices(ixMap);
		for (Node subNode : node.getChildren()) {
			fixIndices(subNode, ixMap);
		}
	}

	public DocumentNode parseInternal(char[] source) {
		ParsingResult<Node> result = parseToParsingResult(source);
		if (result.hasErrors()) {
			throw new RuntimeException(
					"Internal error during AsciiDoc parsing:\n--- ParseErrors ---\n"
							+ printParseErrors(result)/*
													 * + "\n--- ParseTree ---\n"
													 * + printNodeTree(result)
													 */
			);
		}
		return (DocumentNode) result.resultValue;
	}

	ParsingResult<Node> parseToParsingResult(char[] source) {
		parsingStartTimeStamp = System.currentTimeMillis();
		return parseRunnerProvider.get(Document()).run(source);
	}

	protected boolean checkForParsingTimeout() {
		if (System.currentTimeMillis() - parsingStartTimeStamp > maxParsingTimeInMillis)
			throw new ParsingTimeoutException();
		return true;
	}

	protected interface SuperNodeCreator {
		SuperNode create(Node child);
	}
}