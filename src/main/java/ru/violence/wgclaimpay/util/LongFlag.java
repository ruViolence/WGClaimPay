package ru.violence.wgclaimpay.util;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;

public class LongFlag extends Flag<Long> {
    public LongFlag(String name, RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    public LongFlag(String name) {
        super(name);
    }

    @Override
    public Long parseInput(FlagContext context) throws InvalidFlagFormat {
        String input = context.getUserInput();
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            throw new InvalidFlagFormat("Not a long: " + input);
        }
    }

    @Override
    public Long unmarshal(Object o) {
        if (o instanceof Long) {
            return (Long) o;
        } else {
            return o instanceof Number ? ((Number) o).longValue() : null;
        }
    }

    @Override
    public Object marshal(Long o) {
        return o;
    }
}
