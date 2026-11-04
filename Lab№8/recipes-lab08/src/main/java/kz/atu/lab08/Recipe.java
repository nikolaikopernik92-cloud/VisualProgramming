package kz.atu.lab08;

/** Модель записи: один рецепт. */
public class Recipe {
    private String title;
    private String ingredients;
    private String steps;

    public Recipe(String title, String ingredients, String steps) {
        this.title = title;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public String getTitle() { return title; }
    public String getIngredients() { return ingredients; }
    public String getSteps() { return steps; }

    /** ListView показывает название рецепта. */
    @Override
    public String toString() { return title; }
}
