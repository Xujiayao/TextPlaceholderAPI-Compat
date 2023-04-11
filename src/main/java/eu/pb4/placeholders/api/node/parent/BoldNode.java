package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Arrays;

public final class BoldNode extends ParentNode {
    private final boolean value;

    public BoldNode(TextNode[] nodes, boolean value) {
        super(nodes);
        this.value = value;
    }

    @Override
    protected Text applyFormatting(MutableText out, ParserContext context) {
        return out.setStyle(out.getStyle().withBold(this.value));
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new BoldNode(children, this.value);
    }

    @Override
    public String toString() {
        return "BoldNode{" +
                "value=" + value +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}