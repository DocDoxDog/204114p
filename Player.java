package mygame.characters;

public class Player extends CharacterBase {
    private int damage;  // Attack damage
    private int score;   // Player's score

    public Player(String name, int hp, int startX, int startY, int damage) {
        super(name, hp, startX, startY);
        this.damage = damage;
        this.score = 0;
    }

    public void attack(CharacterBase enemy) {
        enemy.takeDamage(damage);
    }

    public void increaseScore(int amount) {
        score += amount;
    }

    public int getScore() {
        return score;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public void update() {
        // For expansions (movement, animations, etc.)
    }
}
