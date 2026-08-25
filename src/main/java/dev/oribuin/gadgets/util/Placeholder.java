package dev.oribuin.gadgets.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface Placeholder {

    /**
     * Get the placeholders for the object
     *
     * @return The resulting placeholders
     */
    default Supplier<Placeholders> getPlaceholders() {
        return Placeholders::empty;
    }
    
}
