package com.mieze.html;

import java.util.List;
import java.util.ArrayList;

public class HTMLBuilder {
    private HTMLHeadElement head;
    private HTMLBodyElement body;
    
    public HTMLBuilder() {
        this.head = new HTMLHeadElement();
        this.body = new HTMLBodyElement();
    }
    
    public HTMLBuilder addToHead(final HTMLElement ele) {
        this.head.add(ele);
        return this;
    }
    
    public HTMLBuilder addToBody(final HTMLElement ele) {
        this.body.add(ele);
        return this;
    }
    
    @Override
    public String toString() {
        return "<head>\n".indent(4) +
            this.head.toString().indent(8) +
            "</head>\n".indent(4) +
            "<body>\n".indent(4) +
            this.body.toString().indent(8) +
            "</body>\n".indent(4);
    }

    public class HTMLElement {
        private List<String> arguments;
        private String name;
        
        public HTMLElement(final String... args) {
            this.arguments = new ArrayList<String>();
            this.name = "element";
            for (int length = args.length, i = 0; i < length; ++i) {
                this.arguments.add(args[i]);
            }
        }
        
        @Override
        public String toString() {
            String s = "";
            for (String str : arguments) {
                s += str;
            }
            return '<' + this.name + ' ' + s.substring(0, s.length() - 1) + '>';
        }
    }

    public abstract class HTMLGroupElement extends HTMLElement {
        private List<HTMLElement> children;
        
        protected abstract String getName();
        
        private HTMLGroupElement(final String... args) {
            super(args);
            this.children = new ArrayList<HTMLElement>();
        }
        
        public HTMLGroupElement add(final HTMLElement htmlElement) {
            this.children.add(htmlElement);
            return this;
        }
        
        @Override
        public String toString() {
            return super.toString() + '\n' + String.join("\n", this.children.stream().map(HTMLElement::toString).toList().toArray(new String[0])) + "</" + this.getName() + '>';
        }
    }

    
    public class HTMLHeadElement extends HTMLGroupElement {
        protected String getName() {
            return "head";
        }
    
        public HTMLHeadElement(final String... args) {
            super(args);
        }
    }

    public class HTMLBodyElement extends HTMLGroupElement {
        protected String getName() {
            return "head";
        }
    
        public HTMLBodyElement(final String... args) {
            super(args);
        }
    }

}
