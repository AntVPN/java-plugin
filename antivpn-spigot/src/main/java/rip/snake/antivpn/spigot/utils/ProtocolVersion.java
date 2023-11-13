//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package rip.snake.antivpn.spigot.utils;

import com.google.common.collect.Maps;

import java.util.Map;

public enum ProtocolVersion {
    UNKNOWN(-1, new String[]{"Unknown"}),
    LEGACY(-2, new String[]{"Legacy"}),
    MINECRAFT_1_7_2(4, new String[]{"1.7.2", "1.7.3", "1.7.4", "1.7.5"}),
    MINECRAFT_1_7_6(5, new String[]{"1.7.6", "1.7.7", "1.7.8", "1.7.9", "1.7.10"}),
    MINECRAFT_1_8(47, new String[]{"1.8", "1.8.1", "1.8.2", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9"}),
    MINECRAFT_1_9(107, new String[]{"1.9"}),
    MINECRAFT_1_9_1(108, new String[]{"1.9.1"}),
    MINECRAFT_1_9_2(109, new String[]{"1.9.2"}),
    MINECRAFT_1_9_4(110, new String[]{"1.9.3", "1.9.4"}),
    MINECRAFT_1_10(210, new String[]{"1.10", "1.10.1", "1.10.2"}),
    MINECRAFT_1_11(315, new String[]{"1.11"}),
    MINECRAFT_1_11_1(316, new String[]{"1.11.1", "1.11.2"}),
    MINECRAFT_1_12(335, new String[]{"1.12"}),
    MINECRAFT_1_12_1(338, new String[]{"1.12.1"}),
    MINECRAFT_1_12_2(340, new String[]{"1.12.2"}),
    MINECRAFT_1_13(393, new String[]{"1.13"}),
    MINECRAFT_1_13_1(401, new String[]{"1.13.1"}),
    MINECRAFT_1_13_2(404, new String[]{"1.13.2"}),
    MINECRAFT_1_14(477, new String[]{"1.14"}),
    MINECRAFT_1_14_1(480, new String[]{"1.14.1"}),
    MINECRAFT_1_14_2(485, new String[]{"1.14.2"}),
    MINECRAFT_1_14_3(490, new String[]{"1.14.3"}),
    MINECRAFT_1_14_4(498, new String[]{"1.14.4"}),
    MINECRAFT_1_15(573, new String[]{"1.15"}),
    MINECRAFT_1_15_1(575, new String[]{"1.15.1"}),
    MINECRAFT_1_15_2(578, new String[]{"1.15.2"}),
    MINECRAFT_1_16(735, new String[]{"1.16"}),
    MINECRAFT_1_16_1(736, new String[]{"1.16.1"}),
    MINECRAFT_1_16_2(751, new String[]{"1.16.2"}),
    MINECRAFT_1_16_3(753, new String[]{"1.16.3"}),
    MINECRAFT_1_16_4(754, new String[]{"1.16.4", "1.16.5"}),
    MINECRAFT_1_17(755, new String[]{"1.17"}),
    MINECRAFT_1_17_1(756, new String[]{"1.17.1"}),
    MINECRAFT_1_18(757, new String[]{"1.18", "1.18.1"}),
    MINECRAFT_1_18_2(758, new String[]{"1.18.2"}),
    MINECRAFT_1_19(759, new String[]{"1.19"}),
    MINECRAFT_1_19_1(760, new String[]{"1.19.1", "1.19.2"}),
    MINECRAFT_1_19_3(761, new String[]{"1.19.3"}),
    MINECRAFT_1_19_4(762, new String[]{"1.19.4"}),
    MINECRAFT_1_20(763, new String[]{"1.20", "1.20.1"}),
    MINECRAFT_1_20_2(764, new String[]{"1.20.2"});

    private static final Map<String, ProtocolVersion> VERSION_TO_PROTOCOL_CONSTANT = Maps.newConcurrentMap();

    static {
        for (ProtocolVersion value : values()) {
            for (String name : value.names) {
                VERSION_TO_PROTOCOL_CONSTANT.put(name, value);
            }
        }
    }

    private final int protocol;
    private final int snapshotProtocol;
    private final String[] names;

    private ProtocolVersion(int protocol, String... names) {
        this(protocol, -1, names);
    }

    private ProtocolVersion(int protocol, int snapshotProtocol, String... names) {
        if (snapshotProtocol != -1) {
            this.snapshotProtocol = 1073741824 | snapshotProtocol;
        } else {
            this.snapshotProtocol = -1;
        }

        this.protocol = protocol;
        this.names = names;
    }

    public int getProtocol() {
        return this.protocol == -1 ? this.snapshotProtocol : this.protocol;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public boolean isLegacy() {
        return this == LEGACY;
    }

    public static ProtocolVersion getProtocolVersion(String version) {
        return VERSION_TO_PROTOCOL_CONSTANT.getOrDefault(version, ProtocolVersion.UNKNOWN);
    }
}
