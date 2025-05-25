package tests;

import model.WordSelector;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WordSelectorTest {

    WordSelector selector = new WordSelector();

    @Test
    void testCanFormWord_True() {
        assertTrue(selector.canFormWord("examination", "name"));
    }

    @Test
    void testCanFormWord_False() {
        assertFalse(selector.canFormWord("hello", "world"));
    }

    @Test
    void testSelectSequence_EmptyInput() {
        List<String> result = selector.selectSequence(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void testSelectSequence_NoLongWord() {
        List<String> input = List.of("cat", "dog", "apple", "zebra");
        List<String> result = selector.selectSequence(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSelectSequence_CorrectStructure() {
        List<String> input = new ArrayList<>(List.of(
                "examination", // >=10 символов
                "name", "man", "axe", "exam", "nation", "tone", "notion",
                "apple", "banana", "carrot", "dragon", "echo", "falcon"
        ));

        List<String> result = selector.selectSequence(input);

        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 4); // longWord + ≤3 составных + ≤7 случайных
        assertEquals(result.get(0), "examination");
    }

    @Test
    void testSelectSequence_NoFormedWords() {
        List<String> input = List.of(
                "abcdefghijklmnop", // длинное
                "xyz", "uvw", "123", "test" // ничего не составляется
        );

        List<String> result = selector.selectSequence(input);

        assertEquals("abcdefghijklmnop", result.get(0));
        // может содержать только longWord + до 7 случайных
        assertTrue(result.size() >= 1 && result.size() <= 8);
    }

    @Test
    void testFindsExactly10Words_IfPossible() {
        List<String> input = new ArrayList<>();
        input.add("examination"); // long word
        input.addAll(List.of("name", "man", "exam")); // составимые
        for (int i = 0; i < 20; i++) {
            input.add("word" + i);
        }

        List<String> result = selector.selectSequence(input);
        assertEquals(11, result.size()); // 1 longWord + 3 + 7 = 11
    }

    @Test
    void testRandomness_DifferentRunsGiveDifferentResults() {
        List<String> input = new ArrayList<>();
        input.add("examination");
        for (int i = 0; i < 100; i++) {
            input.add("word" + i);
        }

        List<String> result1 = selector.selectSequence(input);
        List<String> result2 = selector.selectSequence(input);

        // Обычно последовательности будут отличаться
        assertNotEquals(result1, result2);
    }
}
