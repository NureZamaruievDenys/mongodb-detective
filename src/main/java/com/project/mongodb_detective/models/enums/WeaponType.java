package com.project.mongodb_detective.models.enums;

public enum WeaponType {
    PISTOL("вогнепальним пораненням калібру 9мм"),
    KNIFE("множинними пораненнями ножем"),
    SHOTGUN("вогнепальним пораненням дробового типу боєприпасу"),
    RIFLE("вогнепальним пораненням з гвинтівки"),
    OTHER("інше");

    private final String description;

    WeaponType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
