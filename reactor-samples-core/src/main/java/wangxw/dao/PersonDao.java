package wangxw.dao;

import reactor.core.publisher.Flux;
import wangxw.entity.Person;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wangxw
 * @Date: 2021/09/06
 * @Description:
 */
public class PersonDao {
    /**
     * 阻塞式的
     * @return
     */
    public List<Person> listPeople() {
        return query("select * from people");
    }

    /**
     * 反应式的
     * @return
     */
    public Flux<Person> rxListPeople() {
        return Flux.fromIterable(query("select * from people"));
    }

    private List<Person> query(String sql) {
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Person person = new Person();
            person.setId(i);
            people.add(person);
        }
        return people;
    }
}
