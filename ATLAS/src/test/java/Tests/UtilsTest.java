package Tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import Utils.Utils;

public class UtilsTest {
	
	@Test
    void testPrepareThreadIntervalsPerfectDivision() {
        // Arrange
        int numberOfChunks = 4;
        int regionStart = 0;
        int regionEnd = 400;
        
        // Act
        TreeMap<Integer, Integer> result = Utils.prepareThreadIntervals(numberOfChunks, regionStart, regionEnd);  // Replace MyClass with the actual class

        // Assert
        assertEquals(4, result.size()); // Expecting 4 chunks
        assertEquals(100, result.get(0));  // First interval (0 - 100)
        assertEquals(200, result.get(100));  // Second interval (100 - 200)
        assertEquals(300, result.get(200));  // Third interval (200 - 300)
        assertEquals(400, result.get(300));  // Fourth interval (300 - 400)
    }
	
	@Test
    void testPrepareThreadIntervalsWithRemainder() {
        // Arrange
        int numberOfChunks = 3;
        int regionStart = 0;
        int regionEnd = 350;

        // Act
        TreeMap<Integer, Integer> result = Utils.prepareThreadIntervals(numberOfChunks, regionStart, regionEnd);

        // Assert
        assertEquals(3, result.size()); // Expecting 3 chunks
        assertEquals(116, result.get(0));  // First interval (0 - 116)
        assertEquals(232, result.get(116));  // Second interval (116 - 232)
        assertEquals(350, result.get(232));  // Third interval (232 - 350)
    }
	
	@Test
    void testPrepareThreadIntervalsWithSmallRemainder() {
        // Arrange
        int numberOfChunks = 3;
        int regionStart = 0;
        int regionEnd = 305;

        // Act
        TreeMap<Integer, Integer> result = Utils.prepareThreadIntervals(numberOfChunks, regionStart, regionEnd);

        // Assert
        assertEquals(3, result.size()); // Expecting 3 chunks
        assertEquals(101, result.get(0));  // First interval (0 - 101)
        assertEquals(202, result.get(101));  // Second interval (101 - 202)
        assertEquals(305, result.get(202));  // Third interval (202 - 305)
    }
	
	@Test
    void testPrepareThreadIntervalsOneChunk() {
        // Arrange
        int numberOfChunks = 1;
        int regionStart = 0;
        int regionEnd = 100;

        // Act
        TreeMap<Integer, Integer> result = Utils.prepareThreadIntervals(numberOfChunks, regionStart, regionEnd);

        // Assert
        assertEquals(1, result.size()); // Expecting 1 chunk
        assertEquals(100, result.get(0));  // Only interval (0 - 100)
    }
	
	@Test
    void testPrepareThreadIntervalsZeroChunks() {
        // Arrange
        int numberOfChunks = 0;
        int regionStart = 0;
        int regionEnd = 100;

        // Act and Assert
        assertThrows(ArithmeticException.class, () -> {
        	Utils.prepareThreadIntervals(numberOfChunks, regionStart, regionEnd);
        });
    }

}
