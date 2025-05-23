package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WordSelector {

    /**
     * Возвращает последовательность слов по заданным правилам:
     * 1. Первое слово длиной >=10 с случайной позиции.
     * 2. Три слова, которые можно составить из букв первого.
     * 3. Семь случайных слов, не совпадающих с уже найденными.
     *
     * Если не хватает слов, возвращает столько, сколько смогло найти.
     */
    public List<String> selectSequence(List<String> words) {
        List<String> result = new ArrayList<>();
        if (words == null || words.isEmpty()) {
            return result;
        }

        String longWord = selectLongWordFromRandomPosition(words);
        if (longWord == null) {
            // Не найдено слово длиной >= 10
            return result;
        }

        Set<String> foundWords = new HashSet<>();
        result.add(longWord);
        foundWords.add(longWord);

        // Ищем 3 слова, составленных из букв longWord
        for (int i = 0; i < 3; i++) {
            String formedWord = findWordFromLetters(words, longWord, foundWords);
            if (formedWord != null) {
                result.add(formedWord);
                foundWords.add(formedWord);
            } else {
                // Если не найдено, просто пропускаем
                break;
            }
        }

        // Ищем 7 случайных слов, не совпадающих с уже найденными
        List<String> randomWords = findRandomWords(words, 7, foundWords);
        result.addAll(randomWords);

        return result;
    }

    private String selectLongWordFromRandomPosition(List<String> words) {
        if (words.isEmpty()) return null;

        Random random = new Random();
        int startIndex = random.nextInt(words.size());

        for (int i = startIndex; i < words.size(); i++) {
            String word = words.get(i);
            if (word.length() >= 10) {
                return word;
            }
        }
        for (int i = 0; i < startIndex; i++) {
            String word = words.get(i);
            if (word.length() >= 10) {
                return word;
            }
        }
        return null;
    }

    public boolean canFormWord(String source, String target) {
        Map<Character, Integer> sourceCount = getCharCountMap(source);
        Map<Character, Integer> targetCount = getCharCountMap(target);

        for (Map.Entry<Character, Integer> entry : targetCount.entrySet()) {
            char ch = entry.getKey();
            int needed = entry.getValue();
            int available = sourceCount.getOrDefault(ch, 0);
            if (available < needed) {
                return false;
            }
        }
        return true;
    }

    private Map<Character, Integer> getCharCountMap(String word) {
        Map<Character, Integer> countMap = new HashMap<>();
        for (char ch : word.toCharArray()) {
            countMap.put(ch, countMap.getOrDefault(ch, 0) + 1);
        }
        return countMap;
    }

    private String findWordFromLetters(List<String> words, String baseWord, Set<String> excludeWords) {
        if (words.isEmpty()) return null;

        Random random = new Random();
        int startIndex = random.nextInt(words.size());

        for (int i = startIndex; i < words.size(); i++) {
            String candidate = words.get(i);
            if (candidate.length() <= baseWord.length() && !excludeWords.contains(candidate) && canFormWord(baseWord, candidate)) {
                return candidate;
            }
        }
        for (int i = 0; i < startIndex; i++) {
            String candidate = words.get(i);
            if (candidate.length() <= baseWord.length() && !excludeWords.contains(candidate) && canFormWord(baseWord, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private List<String> findRandomWords(List<String> words, int count, Set<String> excludeWords) {
        List<String> result = new ArrayList<>();
        Random random = new Random();

        Set<String> availableWords = new HashSet<>(words);
        availableWords.removeAll(excludeWords);

        if (availableWords.size() < count) {
            count = availableWords.size();
        }

        List<String> availableList = new ArrayList<>(availableWords);

        while (result.size() < count && !availableList.isEmpty()) {
            int index = random.nextInt(availableList.size());
            String chosen = availableList.remove(index);
            result.add(chosen);
        }

        return result;
    }

    // Для удобного самостоятельного запуска и теста
    public static void main(String[] args) {
        WordSelector selector = new WordSelector();

        try (InputStream is = WordSelector.class.getClassLoader().getResourceAsStream("dictionary/dictionary.txt")) {
            if (is == null) {
                System.err.println("Файл dictionary.txt не найден в ресурсах!");
                return;
            }
            List<String> words;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                words = reader.lines().toList();
            }

            List<String> sequence = selector.selectSequence(words);

            System.out.println("Найденная последовательность слов:");
            for (String w : sequence) {
                System.out.println(w);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
