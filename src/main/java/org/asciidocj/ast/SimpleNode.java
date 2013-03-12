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

package org.asciidocj.ast;

import java.util.List;

import org.parboiled.common.ImmutableList;

public class SimpleNode extends AbstractNode {

	public enum Type {
		Apostrophe, Ellipsis, Emdash, Endash, HRule, Linebreak, Nbsp
	}

	private final Type type;

	public SimpleNode(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public List<Node> getChildren() {
		return ImmutableList.of();
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return super.toString() + " " + type;
	}
}