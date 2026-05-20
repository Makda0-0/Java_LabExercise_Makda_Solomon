package com.template;

import java.util.*;
import java.util.stream.Collectors;


/*
  HandEvaluator - A utility class that evaluates poker hands
  Determines the strength of 5-card and 7-card poker hands
  Used to compare which player has the winning hand
 */




public class HandEvaluator {

    //Rules
    public enum HandRank {
        HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH, ROYAL_FLUSH
    }

    public static class EvaluationResult implements Comparable<EvaluationResult> {
        public HandRank rank;// type of hand
        public int scoreValue;

        public EvaluationResult(HandRank rank, int scoreValue) {
            this.rank = rank;
            this.scoreValue = scoreValue;
        }

        @Override
        public int compareTo(EvaluationResult o) {
            if (this.rank.ordinal() != o.rank.ordinal()) {
                return Integer.compare(this.rank.ordinal(), o.rank.ordinal());
            }
            return Integer.compare(this.scoreValue, o.scoreValue);
        }
    }

    public static EvaluationResult evaluate7CardHand(List<Card> hand) {
        List<List<Card>> combinations = new ArrayList<>();
        generateCombinations(hand, new ArrayList<>(), 0, combinations);

        EvaluationResult bestResult = new EvaluationResult(HandRank.HIGH_CARD, 0);
        for (List<Card> combo : combinations) {
            EvaluationResult current = evaluate5CardHand(combo);
            if (current.compareTo(bestResult) > 0) {
                bestResult = current;
            }
        }
        return bestResult;
    }

    private static void generateCombinations(List<Card> source, List<Card> current, int index, List<List<Card>> results) {
        if (current.size() == 5) {
            results.add(new ArrayList<>(current));
            return;
        }
        if (index >= source.size()) return;

        current.add(source.get(index));
        generateCombinations(source, current, index + 1, results);
        current.remove(current.size() - 1);
        generateCombinations(source, current, index + 1, results);
    }

    private static EvaluationResult evaluate5CardHand(List<Card> fiveCards) {
        fiveCards.sort((c1, c2) -> Integer.compare(c2.getValue(), c1.getValue()));

        boolean isFlush = fiveCards.stream().map(Card::getSuit).distinct().count() == 1;
        boolean isStraight = true;

        for (int i = 0; i < 4; i++) {
            if (fiveCards.get(i).getValue() - fiveCards.get(i + 1).getValue() != 1) {
                isStraight = false;
                break;
            }
        }

        if (!isStraight && fiveCards.get(0).getValue() == 14 && fiveCards.get(1).getValue() == 5
                && fiveCards.get(2).getValue() == 4 && fiveCards.get(3).getValue() == 3 && fiveCards.get(4).getValue() == 2) {
            isStraight = true;
        }


        Map<Integer, Long> counts = fiveCards.stream().collect(Collectors.groupingBy(Card::getValue, Collectors.counting()));
        List<Long> frequencies = counts.values().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        int highCardValue = fiveCards.get(0).getValue();



        if (isStraight && isFlush) {
            if (fiveCards.get(0).getValue() == 14 && fiveCards.get(1).getValue() == 13)
                return new EvaluationResult(HandRank.ROYAL_FLUSH, highCardValue);
            return new EvaluationResult(HandRank.STRAIGHT_FLUSH, highCardValue);
        }

        if (frequencies.get(0) == 4) return new EvaluationResult(HandRank.FOUR_OF_A_KIND, highCardValue);

        if (frequencies.get(0) == 3 && frequencies.get(1) == 2) return new EvaluationResult(HandRank.FULL_HOUSE, highCardValue);

        if (isFlush) return new EvaluationResult(HandRank.FLUSH, highCardValue);

        if (isStraight) return new EvaluationResult(HandRank.STRAIGHT, highCardValue);

        if (frequencies.get(0) == 3) return new EvaluationResult(HandRank.THREE_OF_A_KIND, highCardValue);

        if (frequencies.get(0) == 2 && frequencies.get(1) == 2) return new EvaluationResult(HandRank.TWO_PAIR, highCardValue);

        if (frequencies.get(0) == 2) return new EvaluationResult(HandRank.PAIR, highCardValue);


        return new EvaluationResult(HandRank.HIGH_CARD, highCardValue);
    }
}