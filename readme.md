Java Micro ORM


## Why Femto-ORM
- [Related document](https://stackoverflow.com/questions/6494938/java-micro-orm-equivalent)

Femto-ORM provides a simple way to map your POJO with relational database instead of creating SQL.
And you also don't want Hibernate or Spring Data to bring you free features you don't need.

If you are going to find a total solution, please choose Hibernate or Spring Data.

## Samples

```java
import lombok.Data;

@Data
public class Device {
    
    private int id;
    
    private String model;
    
    private String productNo;
    
    private String uuid;
}
```

```java
import org.femtoframework.orm.Repository;
import org.femtoframework.orm.RepositoryException;

public class DeviceServiceImpl implements DeviceService {
    
    @Inject
    Repository<Device> repository;
    
    public Device getDevice(int id) throws RepositoryException {
        return repository.getById(id);
    }
    
    public List<Device> listAllDevices() throws RepositoryException {
        return repository.listAll();
    }
    
    public boolean addDevice(Device device) throws RepositoryException {
        return repository.create(device);
    }
    
    public List<Device> searchDevicesByProductNo(String key) throws RepositoryException {
        return repository.listBy("product_no LIKE '%?%'", key);
    }
    
    public List<Device> deleteDevice(int id) throws RepositoryException {
        return repository.deleteById(id);
    }
}


```