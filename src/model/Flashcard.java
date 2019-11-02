package model;

public class Flashcard {
    //private instance variables
    private String foreignTerm;
    private String definition;

    //constructor which initialises the instance variables
    public Flashcard(String foreignTerm, String definition) {
        this.foreignTerm = foreignTerm;
        this.definition = definition;
    }

    //getter and setter methods for better encapsulation and information hiding (more secure)
    public String getTerm() {
        return foreignTerm;
    }

    public void setTerm(String foreignTerm) {
        this.foreignTerm = foreignTerm;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
}
