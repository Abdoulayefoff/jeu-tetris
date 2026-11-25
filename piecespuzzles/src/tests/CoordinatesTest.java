import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import piece.Coordinates;

class CoordinatesTest {

    @Test
    void equal_coordinates_are_equal_and_share_hash() {
        Coordinates a = new Coordinates(2, 7);
        Coordinates b = new Coordinates(2, 7);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void different_coordinates_are_not_equal() {
        assertNotEquals(new Coordinates(2, 7), new Coordinates(7, 2));
        assertNotEquals(new Coordinates(2, 7), null);
        assertNotEquals(new Coordinates(2, 7), "2,7");
    }

    @Test
    void works_in_hash_sets() {
        Set<Coordinates> set = new HashSet<>();
        set.add(new Coordinates(1, 1));
        set.add(new Coordinates(1, 1));
        assertEquals(1, set.size());
        assertTrue(set.contains(new Coordinates(1, 1)));
    }
}
