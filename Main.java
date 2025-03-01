package mygame;

public class Main {
    public static int bestScore = 0; // We'll still keep a static variable to hold it in memory

    public static void main(String[] args) {
        // 1) Read best score from file
        bestScore = HighScoreManager.readBestScore();

        // 2) Create Lobby
        LobbyScreen lobby = new LobbyScreen();
        // Display the bestScore we loaded
        lobby.setBestScore(bestScore);
        lobby.setVisible(true);
    }
}
