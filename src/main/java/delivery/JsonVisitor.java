package delivery;

import java.util.stream.Collectors;

final class JsonVisitor implements Visitor<String> {
    private String group(String type, MenuGroup node) {
        String children = node.children().stream().map(child -> child.accept(this)).collect(Collectors.joining(","));
        return "{\"type\":\"" + type + "\",\"name\":\"" + escape(node.name()) + "\",\"children\":[" + children + "]}";
    }
    private String leaf(String type, String name, int calories, String allergen) {
        return "{\"type\":\"" + type + "\",\"name\":\"" + escape(name) + "\",\"calories\":" + calories + ",\"allergen\":\"" + escape(allergen) + "\"}";
    }
    private String escape(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"') result.append("\\\"");
            else if (c == '\\') result.append("\\\\");
            else if (c < 0x20) result.append(String.format("\\u%04x", (int)c));
            else result.append(c);
        }
        return result.toString();
    }
    public String visit(City node) { return group("city", node); }
    public String visit(Restaurant node) { return group("restaurant", node); }
    public String visit(Menu node) { return group("menu", node); }
    public String visit(Dish node) { return leaf("dish", node.name(), node.calories(), node.allergen()); }
    public String visit(Combo node) { return leaf("combo", node.name(), node.calories(), node.allergen()); }
    public String visit(Drink node) { return leaf("drink", node.name(), node.calories(), node.allergen()); }
    public String visit(Dessert node) { return leaf("dessert", node.name(), node.calories(), node.allergen()); }
    public String visit(Side node) { return leaf("side", node.name(), node.calories(), node.allergen()); }
}
