package model;

public class TheoryQuestion {
    //instance variables of a question of music theory
    private String question;
    private String[] answers = new String[4];
    private int answerIndex;

    //initialise the variables
    public TheoryQuestion(String question, String[] answers, int answerIndex) {
        this.question = question;
        this.answers = answers;
        this.answerIndex = answerIndex;
    }

    public void setAnswers(String a1, String a2, String a3, String a4) {
        answers[0] = a1;
        answers[1] = a2;
        answers[2] = a3;
        answers[3] = a4;
    }

    public void setIndex(int index) {
        answerIndex = index;
    }

    public String getQuestion() {
        return question;
    }

    //getter and setter methods for the instance variables
    public void setQuestion(String question) {
        this.question = question;
    }

    public String[] getAnswers() {
        return answers;
    }

    public String getAnswer(int index) {
        return answers[index];
    }

    public int getAnswerIndex() {
        return answerIndex;
    }
}
