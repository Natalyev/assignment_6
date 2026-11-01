package delivery;

interface Visitor<R> {
    R visit(City node);
    R visit(Restaurant node);
    R visit(Menu node);
    R visit(Dish node);
    R visit(Combo node);
    R visit(Drink node);
    R visit(Dessert node);
    R visit(Side node);
}
