package wangxw.service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import wangxw.entity.Flight;
import wangxw.entity.Passenger;
import wangxw.entity.Ticket;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wangxw
 * @Date: 2021/09/07
 * @Description:
 */
@Slf4j
public class TicketService {
    /**
     * 查询航班
     *
     * @param flightNo
     * @return
     */
    public Flight lookupFlight(String flightNo) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Flight(flightNo);
    }

    public Mono<Flight> rxLookupFlight(String flightNo) {
        return Mono.defer(() ->
                Mono.just(lookupFlight(flightNo)));
    }

    /**
     * 查询乘客
     *
     * @param id
     * @return
     */
    public Passenger findPassenger(Long id) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Passenger(id);
    }

    public Mono<Passenger> rxFindPassenger(Long id) {
        return Mono.defer(() ->
                Mono.just(findPassenger(id)));
    }

    /**
     * 根据航班和乘客订票
     *
     * @param flight
     * @param passenger
     * @return
     */
    public Ticket bookTicket(Flight flight, Passenger passenger) {
        return new Ticket(flight, passenger);
    }

    public Mono<Ticket> rxBookTicket(Flight flight, Passenger passenger) {
        return Mono.defer(() ->
                Mono.just(bookTicket(flight, passenger)));
    }

    /**
     * 发送邮件
     *
     * @param ticket
     * @return
     */
    public boolean sendEmail(Ticket ticket) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("send email {}", ticket);
        return true;
    }

    ExecutorService pool = Executors.newFixedThreadPool(3);


    public Future<Boolean> sendEmailAsync(Ticket ticket) {
        return pool.submit(() -> sendEmail(ticket));
    }

    public Mono<Boolean> rxSendEmail(Ticket ticket){
        return Mono.fromCallable(() -> sendEmail(ticket));
    }

}
