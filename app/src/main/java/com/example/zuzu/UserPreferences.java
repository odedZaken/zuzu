package com.example.zuzu;

public class UserPreferences {

    private Boolean isPrefSoccer, isPrefBasketball, isPrefVolleyball, isPrefRunning, isPrefTennis, isPrefExercise;

    public UserPreferences() {
        this.isPrefSoccer = true;
        this.isPrefBasketball = true;
        this.isPrefVolleyball = true;
        this.isPrefRunning = true;
        this.isPrefTennis = true;
        this.isPrefExercise = true;
    }

    public UserPreferences(Boolean isPrefSoccer, Boolean isPrefBasketball, Boolean isPrefVolleyball, Boolean isPrefRunning, Boolean isPrefTennis, Boolean isPrefExercise) {
        this.isPrefSoccer = isPrefSoccer;
        this.isPrefBasketball = isPrefBasketball;
        this.isPrefVolleyball = isPrefVolleyball;
        this.isPrefRunning = isPrefRunning;
        this.isPrefTennis = isPrefTennis;
        this.isPrefExercise = isPrefExercise;
    }

    public void setPrefSoccer(Boolean prefSoccer) {
        isPrefSoccer = prefSoccer;
    }

    public void setPrefBasketball(Boolean prefBasketball) {
        isPrefBasketball = prefBasketball;
    }

    public void setPrefVolleyball(Boolean prefVolleyball) {
        isPrefVolleyball = prefVolleyball;
    }

    public void setPrefRunning(Boolean prefRunning) {
        isPrefRunning = prefRunning;
    }

    public void setPrefTennis(Boolean prefTennis) {
        isPrefTennis = prefTennis;
    }

    public void setPrefExercise(Boolean prefExercise) {
        isPrefExercise = prefExercise;
    }

    public Boolean getPrefSoccer() {
        return isPrefSoccer;
    }

    public Boolean getPrefBasketball() {
        return isPrefBasketball;
    }

    public Boolean getPrefVolleyball() {
        return isPrefVolleyball;
    }

    public Boolean getPrefRunning() {
        return isPrefRunning;
    }

    public Boolean getPrefTennis() {
        return isPrefTennis;
    }

    public Boolean getPrefExercise() {
        return isPrefExercise;
    }

    public boolean isPrefByString(String sport) {
        return sport.equals("Soccer") && isPrefSoccer || sport.equals("Basketball") && isPrefBasketball || sport.equals("Running") && isPrefRunning ||
                sport.equals("Tennis") && isPrefTennis || sport.equals("Volleyball") && isPrefVolleyball || sport.equals("Exercise") && isPrefExercise;
    }
}
