package mrg.data;

import mrg.data.auto._CESDomainMap;

public class CESDomainMap extends _CESDomainMap {

    private static CESDomainMap instance;

    private CESDomainMap() {}

    public static CESDomainMap getInstance() {
        if(instance == null) {
            instance = new CESDomainMap();
        }

        return instance;
    }
}
