package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

public class ClearService {
    private final DataAccess clearData;

    public ClearService(DataAccess dataAccess)
    {
        this.clearData = dataAccess;
    }

    public void clear() throws DataAccessException
    {
        clearData.clear();
    }

}
