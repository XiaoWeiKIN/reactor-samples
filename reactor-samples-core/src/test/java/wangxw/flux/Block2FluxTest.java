package wangxw.flux;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import wangxw.entity.Flight;
import wangxw.entity.Passenger;
import wangxw.entity.Person;
import wangxw.dao.PersonDao;
import wangxw.entity.Ticket;
import wangxw.service.TicketService;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: wangxw
 * @Date: 2021/09/06
 * @Description:
 */
@Slf4j
public class Block2FluxTest {

    PersonDao personDao;
    TicketService ticketService;
    List<Ticket> tickets;

    @Before
    public void setUp() {
        personDao = new PersonDao();
        ticketService = new TicketService();
        tickets = new ArrayList<>();
        tickets.add(new Ticket(new Flight("LOT 783"), new Passenger(1L)));
        tickets.add(new Ticket(new Flight("LOT 784"), new Passenger(2L)));
        tickets.add(new Ticket(new Flight("LOT 785"), new Passenger(3L)));

    }

    @Test
    public void listPeopleTest() {
        Flux<Person> peopleFlux = personDao.rxListPeople();
        Flux<List<Person>> listFlux = peopleFlux.buffer()
                .log();
        List<Person> people = listFlux.blockLast(Duration.ofSeconds(3));
        assert people != null;
        people.forEach(person -> log.info(person.toString()));
    }

    @Test
    public void blockBookTicket() throws IOException {
        long start = System.currentTimeMillis();
        Flight flight = ticketService.lookupFlight("LOT 783");
        Passenger passenger = ticketService.findPassenger(1L);
        Ticket ticket = ticketService.bookTicket(flight, passenger);
        ticketService.sendEmail(ticket);
        long end = System.currentTimeMillis() - start;
        System.out.println(end);
    }

    @Test
    public void rxBookTicket() throws IOException {
        long start = System.currentTimeMillis();
        // 只是占位，不产生任何的副作用
        Mono<Flight> flight = ticketService.rxLookupFlight("LOT 783")
                .subscribeOn(Schedulers.boundedElastic());
        Mono<Passenger> passenger = ticketService.rxFindPassenger(1L)
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(3));

//        Mono<Ticket> ticket = flight.zipWith(passenger, (f, p) -> ticketService.bookTicket(f, p));
//        ticket.subscribe(ticketService::sendEmail);

        Mono<Ticket> ticket = flight
                .zipWith(passenger, (f, p) -> ticketService.rxBookTicket(f, p))
                .flatMap(abs -> abs);
        ticketService.sendEmail(ticket.block());

        long end = System.currentTimeMillis() - start;
        System.out.println(end);
    }


    @Test
    public void serial() {
        List<Ticket> faitures = new ArrayList<>();
        for (Ticket ticket : tickets) {
            try {
                ticketService.sendEmail(ticket);
            } catch (IOException e) {
                log.warn("Failed to send {}", ticket, e);
                faitures.add(ticket);
            }
        }
    }

    @Test
    public void pool() {

        List<Pair<Ticket, Future<Boolean>>> tasks = tickets.stream()
                .map(ticket -> Pair.of(ticket, ticketService.sendEmailAsync(ticket)))
                .collect(Collectors.toList());

        List<Ticket> failures = tasks.stream().flatMap(pair -> {
            try {
                Future<Boolean> future = pair.getRight();
                future.get(1, TimeUnit.SECONDS);
                return Stream.empty();
            } catch (Exception e) {
                Ticket ticket = pair.getLeft();
                log.warn("Failed to send {}", ticket, e);
                return Stream.of(ticket);
            }
        }).collect(Collectors.toList());
    }

    @Test
    public void rx() {
        long start = System.currentTimeMillis();
        Flux.fromIterable(tickets)
                .flatMap(ticket -> ticketService.rxSendEmail(ticket)
                        .flatMap(response -> Mono.<Ticket>empty())
                        .doOnError(e -> log.warn("Failed to send {}", ticket, e))
                        .onErrorReturn(ticket)
                        .subscribeOn(Schedulers.boundedElastic()))
                .buffer()
                .blockLast();
        long end = System.currentTimeMillis() - start;
        System.out.println(end);
    }

}
