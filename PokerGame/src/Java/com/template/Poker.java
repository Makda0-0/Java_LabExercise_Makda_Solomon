package com.template;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

// Main Poker class
public class Poker extends Application {

    public enum Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
    public enum RoundState { PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN }

    public static class Card {
        private final Suit suit;
        private final String rank;
        private final int value;
        public Card(Suit suit, String rank, int value) {
            this.suit = suit; this.rank = rank; this.value = value;
        }
        public Suit getSuit() { return suit; }
        public String getRank() { return rank; }
        public int getValue() { return value; }
    }


   // Manages the deck of cards (shuffling, dealing, resetting)
    public static class Deck {
        private final List<Card> cards = new ArrayList<>();
        public Deck() { reset(); }
        public void reset() {
            cards.clear();
            String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
            int[] values = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
            for (Suit suit : Suit.values()) {
                for (int i = 0; i < ranks.length; i++) {
                    cards.add(new Card(suit, ranks[i], values[i]));
                }
            }
            Collections.shuffle(cards);
        }
        public Card dealCard() { return cards.isEmpty() ? null : cards.remove(0); }
    }
//Represents a player (human or computer) with their game state
    public static class Player {
        String name;
        int chips = 1000;
        int currentRoundBet = 0;
        List<Card> holeCards = new ArrayList<>();
        boolean hasFolded = false;
        boolean isComputer;
        public Player(String name, boolean isComputer) {
            this.name = name; this.isComputer = isComputer;
        }
    }

    // Hand Evaluation Engine Helper Classes
    public static class HandScore implements Comparable<HandScore> {
        int typeRank; // 9 = Royal Flush down to 0 = High Card
        String name;
        int tieBreakerValue;

        public HandScore(int typeRank, String name, int tieBreakerValue) {
            this.typeRank = typeRank; this.name = name; this.tieBreakerValue = tieBreakerValue;
        }

        @Override
        public int compareTo(HandScore o) {
            if (this.typeRank != o.typeRank) return Integer.compare(this.typeRank, o.typeRank);
            return Integer.compare(this.tieBreakerValue, o.tieBreakerValue);
        }
    }
//Game state variables  - Track the current game status
    private final Deck deck = new Deck();
    private final List<Player> players = new ArrayList<>();
    private final List<Card> communityCards = new ArrayList<>();
    private int pot = 0;
    private int highestActiveBet = 0;
    private RoundState currentState = RoundState.PRE_FLOP;

    private boolean vsComputerOnly = false;
    private int activePlayerIndex = 0;
    private int consecutivePasses = 0;

    private Stage mainStage;
    private final Label potLabel = new Label();
    private final Label statusLabel = new Label();
    private final HBox p1CardContainer = new HBox(12);
    private final HBox oppCardContainer = new HBox(12);
    private final Label p1StatsLabel = new Label();
    private final Label oppStatsLabel = new Label();
    private final VBox p1Box = new VBox(10);
    private final VBox opponentBox = new VBox(10);
    private final HBox communityBox = new HBox(15);
    private final HBox playerPanel = new HBox(60);

    private Button checkCallBtn;
    private Button raiseBetBtn;
    private Slider betSlider;
    private Label betSliderValLabel;

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;

        p1Box.setAlignment(Pos.CENTER);
        p1Box.getChildren().addAll(p1StatsLabel, p1CardContainer);
        opponentBox.setAlignment(Pos.CENTER);
        opponentBox.getChildren().addAll(oppStatsLabel, oppCardContainer);

        showMenuScene();
    }

// to show menu
    private void showMenuScene() {
        VBox menuRoot = new VBox(20);
        menuRoot.setAlignment(Pos.CENTER);
        menuRoot.setPadding(new Insets(50));
        menuRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #0d2316, #144227);");

        Label titleLabel = new Label("Poker Game : TEXAS HOLD EM");

        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color.web("#d4af37"));

        Button playVsHumanBtn = createStyledButton("PLAYER VS PLAYER ", "#ffffff", "#144227", 260);
        Button playVsCompBtn = createStyledButton("PLAYER VS COMPUTER", "#d4af37", "#0d2316", 260);
        Button helpBtn = createStyledButton("HOW TO PLAY & COMBOS", "#5adbb5", "#0d2316", 260);

        playVsHumanBtn.setOnAction(e -> { vsComputerOnly = false; initGameEngine(); showGameScene(); });
        playVsCompBtn.setOnAction(e -> { vsComputerOnly = true; initGameEngine(); showGameScene(); });
        helpBtn.setOnAction(e -> showHelpDialog());

        menuRoot.getChildren().addAll(titleLabel, playVsHumanBtn, playVsCompBtn, helpBtn);
        mainStage.setScene(new Scene(menuRoot, 1020, 660));
        mainStage.setTitle("Poker Texas Hold em");
        mainStage.show();
    }

    private void showHelpDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(mainStage);
        dialog.setTitle("Rules & Winning Combinations");

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #0d2316;");

        Label header = new Label("GAME RULES & HAND COMBOS");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        header.setTextFill(Color.web("#d4af37"));
// Rules of the game
        Label rulesBody = new Label(
                "1. GAMEPLAY FLOW \n" +
                        " Each player gets 2 hidden cards face-down. 5 Shared community cards are dealt face-up over 3 distinct rounds (Flop, Turn, River).\n\n" +
                        "2. THE CORE COMBO RULE\n" +
                        " To form your hand rank, you evaluate the BEST 5-CARD COMBO among your 2 hole cards and the 5 community cards combined.\n" +
                        " *CRITICAL:* Yes, you can legally make a combination using all 5 community cards if your own 2 cards are weaker than what is on the board!\n\n" +
                        "3. POKER COMBINATIONS (Highest to Lowest):\n" +
                        " • Royal Flush: A, K, Q, J, 10 all belonging to the exact same suit.\n" +
                        " • Straight Flush: 5 sequential cards in a row sharing the same suit.\n" +
                        " • Four of a Kind: 4 cards matching identical rank values.\n" +
                        " • Full House: 3 cards of one value + 2 cards matching another value.\n" +
                        " • Flush: Any 5 random cards sharing the identical suit color completely.\n" +
                        " • Straight: 5 sequential numerical cards in a row across mixed suits.\n" +
                        " • Three of a Kind: 3 matching numerical rank values.\n" +
                        " • Two Pair: Two separate matching numerical couples.\n" +
                        " • One Pair: A single structural duo matching values.\n" +
                        " • High Card: Default fallback card sorted by the highest absolute numerical value."
        );
        rulesBody.setTextFill(Color.WHITE);
        rulesBody.setWrapText(true);
        rulesBody.setFont(Font.font("Arial", 13));

        ScrollPane scrollPane = new ScrollPane(rulesBody);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(380);
        scrollPane.setStyle("-fx-background: #0d2316; -fx-background-color: transparent;");

        Button closeBtn = createStyledButton("DISMISS", "#d4af37", "#0d2316", 140);
        closeBtn.setOnAction(e -> dialog.close());

        content.getChildren().addAll(header, scrollPane, closeBtn);
        dialog.setScene(new Scene(content, 580, 500));
        dialog.showAndWait();
    }

// for game screen
    private void showGameScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 80%, #1a5235, #081c11);");

        VBox topPanel = new VBox(10);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 12; -fx-padding: 15;");

        potLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        potLabel.setTextFill(Color.web("#ffcc00"));
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.web("#e0e0e0"));
        topPanel.getChildren().addAll(potLabel, statusLabel);
        root.setTop(topPanel);

        communityBox.setAlignment(Pos.CENTER);
        communityBox.setPrefHeight(160);
        communityBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2); -fx-background-radius: 20; -fx-border-color: rgba(255,255,255,0.1); -fx-padding: 15;");
        BorderPane.setMargin(communityBox, new Insets(20, 40, 20, 40));
        root.setCenter(communityBox);

        playerPanel.setAlignment(Pos.CENTER);
        root.setBottom(playerPanel);

        VBox controls = new VBox(12);
        controls.setPadding(new Insets(15));
        controls.setAlignment(Pos.CENTER);
        controls.setPrefWidth(220);
        controls.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.05);");

        checkCallBtn = createStyledButton("Check", "#2ecc71", "#ffffff", 180);
        Button foldBtn = createStyledButton("Fold", "#e74c3c", "#ffffff", 180);
        raiseBetBtn = createStyledButton("Raise / Bet", "#e67e22", "#ffffff", 180);

        betSlider = new Slider();
        betSliderValLabel = new Label("Raise Amount: 0");
        betSliderValLabel.setTextFill(Color.web("#5adbb5"));
        betSliderValLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        betSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                betSliderValLabel.setText("Raise Amount: " + newVal.intValue())
        );

        checkCallBtn.setOnAction(e -> handleCheckCallAction());
        foldBtn.setOnAction(e -> handleFoldAction());
        raiseBetBtn.setOnAction(e -> handleRaiseAction());

        Button midGameHelpBtn = createStyledButton("Rules & Help", "#5adbb5", "#0d2316", 180);
        midGameHelpBtn.setOnAction(e -> showHelpDialog());

        controls.getChildren().addAll(checkCallBtn, foldBtn, raiseBetBtn, betSlider, betSliderValLabel, midGameHelpBtn);
        root.setRight(controls);

        mainStage.setScene(new Scene(root, 1040, 680));
        updateUI();
    }

    private void initGameEngine() {
        players.clear();
        players.add(new Player("PLAYER 1 (YOU)", false));
        players.add(vsComputerOnly ? new Player("COMPUTER", true) : new Player("PLAYER 2", false));

        startNewHand();//begin first hand
    }

    public void startNewHand() {
        deck.reset();
        communityCards.clear();
        pot = 0;
        currentState = RoundState.PRE_FLOP;
        activePlayerIndex = 0;
        consecutivePasses = 0;

        for (Player p : players) {
            p.holeCards.clear();
            p.hasFolded = false;
            p.currentRoundBet = 0;
            p.holeCards.add(deck.dealCard());
            p.holeCards.add(deck.dealCard());
        }

        Player p1 = players.get(0);
        Player p2 = players.get(1);

        p1.chips -= 10; p1.currentRoundBet = 10; pot += 10;
        p2.chips -= 20; p2.currentRoundBet = 20; pot += 20;
        highestActiveBet = 20;
    }

    private void advanceRound() {
        consecutivePasses = 0;
        highestActiveBet = 0;
        for (Player p : players) p.currentRoundBet = 0;

        switch (currentState) {
            case PRE_FLOP -> {
                communityCards.add(deck.dealCard());
                communityCards.add(deck.dealCard());
                communityCards.add(deck.dealCard());
                currentState = RoundState.FLOP;
            }
            case FLOP -> { communityCards.add(deck.dealCard()); currentState = RoundState.TURN; }
            case TURN -> { communityCards.add(deck.dealCard()); currentState = RoundState.RIVER; }
            case RIVER -> currentState = RoundState.SHOWDOWN;
            case SHOWDOWN -> startNewHand();
        }
    }
// to check/call
    private void handleCheckCallAction() {
        Player actor = players.get(activePlayerIndex);
        int owed = highestActiveBet - actor.currentRoundBet;
        if (owed > 0) {
            int callAmount = Math.min(actor.chips, owed);
            actor.chips -= callAmount;
            actor.currentRoundBet += callAmount;
            pot += callAmount;
        }
        consecutivePasses++;
        evaluateTurnRouting();
    }

    private void handleFoldAction() {
        players.get(activePlayerIndex).hasFolded = true;
        determineWinner();
    }

    private void handleRaiseAction() {
        Player actor = players.get(activePlayerIndex);
        int raiseTarget = (int) betSlider.getValue();
        int additionalChipsNeeded = raiseTarget - actor.currentRoundBet;

        if (additionalChipsNeeded > 0 && additionalChipsNeeded <= actor.chips) {
            actor.chips -= additionalChipsNeeded;
            actor.currentRoundBet = raiseTarget;
            pot += additionalChipsNeeded;
            highestActiveBet = raiseTarget;
            consecutivePasses = 1;
            evaluateTurnRouting();
        }
    }
// for who's turn is it
    private void evaluateTurnRouting() {
        if (currentState == RoundState.SHOWDOWN) return;
        if (vsComputerOnly) {
            if (consecutivePasses >= 2) { advanceRound(); }
            else { executeAiTurn(); if (consecutivePasses >= 2) advanceRound(); }
            activePlayerIndex = 0;
        } else {
            if (consecutivePasses >= 2) { advanceRound(); activePlayerIndex = 0; }
            else { activePlayerIndex = (activePlayerIndex == 0) ? 1 : 0; }
        }
        updateUI();
    }

    public void executeAiTurn() {
        Player ai = players.get(1);
        if (ai.hasFolded || ai.chips <= 0) { consecutivePasses++; return; }
        int owed = highestActiveBet - ai.currentRoundBet;
        if (owed > 0) {
            int callAmount = Math.min(ai.chips, owed);
            ai.chips -= callAmount;
            ai.currentRoundBet += callAmount;
            pot += callAmount;
        }
        consecutivePasses++;
    }

    private void updateUI() {
        if (!(mainStage.getScene().getRoot() instanceof BorderPane)) return;

        potLabel.setText("POT: " + pot + " CHIPS");
        Player activeUser = players.get(activePlayerIndex);

        statusLabel.setText(currentState == RoundState.SHOWDOWN ?
                "SHOWDOWN!" : "ROUND: " + currentState.name() + " | ACTION: " + activeUser.name);

        int owedAmt = highestActiveBet - activeUser.currentRoundBet;
        checkCallBtn.setText(owedAmt == 0 ? "Check" : "Call (" + Math.min(activeUser.chips, owedAmt) + ")");

        int minRaise = highestActiveBet + 20;
        int maxAvailableStack = activeUser.chips + activeUser.currentRoundBet;
        if (activeUser.chips > 0 && minRaise <= maxAvailableStack) {
            betSlider.setDisable(false); raiseBetBtn.setDisable(false);
            betSlider.setMin(minRaise); betSlider.setMax(maxAvailableStack);
        } else {
            betSlider.setDisable(true); raiseBetBtn.setDisable(true);
        }

        communityBox.getChildren().clear();
        if (communityCards.isEmpty()) {
            Label msg = new Label("AWAITING BLINDS");
            msg.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            msg.setTextFill(Color.web("#5c8a6f"));
            communityBox.getChildren().add(msg);
        } else {
            for (Card c : communityCards) communityBox.getChildren().add(createCardUI(c, false));
        }

        playerPanel.getChildren().clear();
        boolean isP1Turn = (activePlayerIndex == 0 && currentState != RoundState.SHOWDOWN);
        boolean isP2Turn = (activePlayerIndex == 1 && currentState != RoundState.SHOWDOWN);

        updatePlayerNode(p1Box, p1StatsLabel, p1CardContainer, players.get(0), false, isP1Turn);
        updatePlayerNode(opponentBox, oppStatsLabel, oppCardContainer, players.get(1), (players.get(1).isComputer && currentState != RoundState.SHOWDOWN), isP2Turn);

        playerPanel.getChildren().addAll(p1Box, opponentBox);

        if (currentState == RoundState.SHOWDOWN) determineWinner();
    }

    private void updatePlayerNode(VBox box, Label statsLabel, HBox cardsContainer, Player p, boolean hide, boolean active) {
        cardsContainer.getChildren().clear();
        box.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 15; -fx-background-radius: 12; -fx-min-width: 200; " +
                (active ? "-fx-border-color: #ffcc00; -fx-border-width: 2; -fx-border-radius: 12;" : "-fx-border-color: transparent;"));

        statsLabel.setText(p.name + "\n" + p.chips + " Chips (Bet: " + p.currentRoundBet + ")");
        statsLabel.setTextFill(Color.WHITE);
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        if (p.hasFolded) {
            Label f = new Label("FOLDED"); f.setTextFill(Color.RED); cardsContainer.getChildren().add(f);
        } else {
            for (Card c : p.holeCards) cardsContainer.getChildren().add(createCardUI(c, hide));
        }
    }

    private Pane createCardUI(Card card, boolean hideCard) {
        StackPane cardPane = new StackPane();
        cardPane.setPrefSize(64, 90);
        cardPane.setMaxSize(64, 90);

        Rectangle cardShape = new Rectangle(64, 90);
        cardShape.setArcWidth(8); cardShape.setArcHeight(8);

        DropShadow ds = new DropShadow(5, 0, 3, Color.web("#000000", 0.5));
        cardPane.setEffect(ds);

        if (hideCard) {
            cardShape.setFill(new LinearGradient(0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE, new Stop(0, Color.web("#1e3c72")), new Stop(1, Color.web("#2a5298"))));
            cardShape.setStroke(Color.WHITE);
            cardPane.getChildren().add(cardShape);
        } else {
            cardShape.setFill(Color.WHITE);
            cardShape.setStroke(Color.LIGHTGRAY);

            String sym = switch (card.getSuit()) { case HEARTS -> "♥"; case DIAMONDS -> "♦"; case CLUBS -> "♣"; case SPADES -> "♠"; };
            Color col = (card.getSuit() == Suit.HEARTS || card.getSuit() == Suit.DIAMONDS) ? Color.RED : Color.BLACK;

            Label centerSuit = new Label(sym); centerSuit.setFont(Font.font("Arial", 24)); centerSuit.setTextFill(col);
            Label tl = new Label(card.getRank() + "\n" + sym); tl.setFont(Font.font("Arial", FontWeight.BOLD, 11)); tl.setTextFill(col);

            AnchorPane overlays = new AnchorPane();
            AnchorPane.setTopAnchor(tl, 2.0); AnchorPane.setLeftAnchor(tl, 4.0);
            overlays.getChildren().add(tl);

            cardPane.getChildren().addAll(cardShape, centerSuit, overlays);
        }
        return cardPane;
    }

// determines the best 5 cards
    private HandScore evaluateBestHand(List<Card> hole, List<Card> community) {
        List<Card> all = new ArrayList<>(hole);// starts with hole cards
        all.addAll(community);
        if (all.size() < 5) return new HandScore(0, "High Card", 0);

        // Sort pool descending by value
        all.sort((c1, c2) -> Integer.compare(c2.getValue(), c1.getValue()));

        Map<Integer, Long> counts = all.stream().collect(Collectors.groupingBy(Card::getValue, Collectors.counting()));
        Map<Suit, Long> suitCounts = all.stream().collect(Collectors.groupingBy(Card::getSuit, Collectors.counting()));

        boolean isFlush = suitCounts.values().stream().anyMatch(c -> c >= 5);

        // Straight Check
        List<Integer> uniqueVals = all.stream().map(Card::getValue).distinct().collect(Collectors.toList());
        int straightHighCard = -1;
        for (int i = 0; i <= uniqueVals.size() - 5; i++) {
            if (uniqueVals.get(i) - uniqueVals.get(i + 4) == 4) {
                straightHighCard = uniqueVals.get(i);
                break;
            }
        }
        // Wheel check (5, 4, 3, 2, A)
        if (uniqueVals.contains(14) && uniqueVals.contains(5) && uniqueVals.contains(4) && uniqueVals.contains(3) && uniqueVals.contains(2)) {
            if (straightHighCard == -1) straightHighCard = 5;
        }

        boolean isStraight = straightHighCard != -1;

        if (isFlush && isStraight) {
            if (straightHighCard == 14) return new HandScore(9, "Royal Flush", 14);
            return new HandScore(8, "Straight Flush", straightHighCard);
        }
        if (counts.containsValue(4L)) {
            int quadVal = counts.entrySet().stream().filter(e -> e.getValue() == 4).map(Map.Entry::getKey).findFirst().orElse(0);
            return new HandScore(7, "Four of a Kind", quadVal);
        }
        if (counts.containsValue(3L) && counts.containsValue(2L)) {
            int tripVal = counts.entrySet().stream().filter(e -> e.getValue() == 3).map(Map.Entry::getKey).findFirst().orElse(0);
            return new HandScore(6, "Full House", tripVal);
        }
        if (isFlush) {
            int maxVal = all.stream().map(Card::getValue).max(Integer::compare).orElse(0);
            return new HandScore(5, "Flush", maxVal);
        }
        if (isStraight) return new HandScore(4, "Straight", straightHighCard);
        if (counts.containsValue(3L)) {
            int tripVal = counts.entrySet().stream().filter(e -> e.getValue() == 3).map(Map.Entry::getKey).findFirst().orElse(0);
            return new HandScore(3, "Three of a Kind", tripVal);
        }

        long pairsCount = counts.values().stream().filter(v -> v == 2).count();
        if (pairsCount >= 2) {
            int topPair = counts.entrySet().stream().filter(e -> e.getValue() == 2).map(Map.Entry::getKey).max(Integer::compare).orElse(0);
            return new HandScore(2, "Two Pair", topPair);
        }
        if (pairsCount == 1) {
            int pairVal = counts.entrySet().stream().filter(e -> e.getValue() == 2).map(Map.Entry::getKey).findFirst().orElse(0);
            return new HandScore(1, "One Pair", pairVal);
        }

        return new HandScore(0, "High Card (" + all.get(0).getRank() + ")", all.get(0).getValue());
    }

    private void determineWinner() {
        Player winner;
        String combinationName = "Fold Settlement";

        long activeCount = players.stream().filter(p -> !p.hasFolded).count();
        if (activeCount > 1) {
            HandScore p1Score = evaluateBestHand(players.get(0).holeCards, communityCards);
            HandScore p2Score = evaluateBestHand(players.get(1).holeCards, communityCards);

            if (p1Score.compareTo(p2Score) >= 0) {
                winner = players.get(0);
                combinationName = p1Score.name;
            } else {
                winner = players.get(1);
                combinationName = p2Score.name;
            }
        } else {
            winner = players.stream().filter(p -> !p.hasFolded).findFirst().orElse(players.get(0));
            combinationName = "Opponent Folded";
        }

        int payout = pot; winner.chips += pot; pot = 0;
        showOutcomeWindow(winner, payout, combinationName);
    }

    private void showOutcomeWindow(Player winner, int potWon, String combination) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL); stage.initOwner(mainStage);

        VBox layout = new VBox(15); layout.setPadding(new Insets(20)); layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #144227; -fx-border-color: #d4af37; -fx-border-width: 2; -fx-border-radius: 5;");

        Label title = new Label(winner.name + " WINS!");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18)); title.setTextFill(Color.GOLD);

        Label comboLabel = new Label("Winning Combo: " + combination);
        comboLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14)); comboLabel.setTextFill(Color.web("#5adbb5"));

        Label payout = new Label("Chips Collected: " + potWon); payout.setTextFill(Color.WHITE);

        HBox row = new HBox(10); row.setAlignment(Pos.CENTER);
        Button next = createStyledButton("Next Hand", "#2ecc71", "#ffffff", 110);
        Button menu = createStyledButton("Main Menu", "#e67e22", "#ffffff", 110);

        next.setOnAction(e -> { stage.close(); startNewHand(); updateUI(); });
        menu.setOnAction(e -> { stage.close(); showMenuScene(); });

        row.getChildren().addAll(next, menu);
        layout.getChildren().addAll(title, comboLabel, payout, row);
        stage.setScene(new Scene(layout, 380, 210));
        stage.showAndWait();
    }

    private Button createStyledButton(String text, String bgColor, String txColor, double width) {
        Button b = new Button(text);
        b.setPrefWidth(width);
        b.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + txColor + "; -fx-background-radius: 6; -fx-padding: 8 0; -fx-cursor: hand; -fx-font-weight: bold;");
        return b;
    }

    public static void main(String[] args) {

        launch(args);
    }
}