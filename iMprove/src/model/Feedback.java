package model;

public class Feedback {
    //encapsulated object: instance variables + respective getter and setter methods
    private String question;
    private String wrongAnswer;
    private String correctAnswer;

    public Feedback(String question, String wrong, String correct) {
        this.question = question;
        wrongAnswer = wrong;
        correctAnswer = correct;
    }

    public String getQuestion() {
        return question;
    }

    public String getWrong() {
        return wrongAnswer;
    }

    public String getCorrect() {
        return correctAnswer;
    }
}
