package delivery;

final class CaloriesVisitor implements Visitor<Integer> {
    private int sum(MenuGroup group) { return group.children().stream().mapToInt(child -> child.accept(this)).sum(); }
    public Integer visit(City node) { return sum(node); }
    public Integer visit(Restaurant node) { return sum(node); }
    public Integer visit(Menu node) { return sum(node); }
    public Integer visit(Dish node) { return node.calories(); }
    public Integer visit(Combo node) { return node.calories(); }
    public Integer visit(Drink node) { return node.calories(); }
    public Integer visit(Dessert node) { return node.calories(); }
}
