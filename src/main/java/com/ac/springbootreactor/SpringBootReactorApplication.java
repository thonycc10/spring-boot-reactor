package com.ac.springbootreactor;

import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) {

        Flux<User> nombres = Flux.just("Andres Ape", "Pedro Ape1", "Maria Ape1", "Diego Ape3")
                .map(nombre -> {
                    return new User(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase());
                })
                .filter(user -> user.getLastName().toLowerCase().equals("ape1"))
                .doOnNext(user -> {
                    if (user == null) {
                        throw new RuntimeException("Se encontro un nombre vacio");
                    }
                    System.out.println(user.getName());
                })
                .map(user -> {
                    user.setName(user.getName().toLowerCase());
                    return user;
                });

        // explesion lambda
//		Flux<String> nombres = Flux.just("Andres", "Pedro", "Juan", "Diego").doOnNext(System.out::println);

        nombres.subscribe(user -> log.info(user.getName()),
                error -> log.error(error.getMessage()),
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("Ha finalizado la ejecución del observable con exito");
                    }
                });

//		explesion lambda
//		nombres.subscribe(log::info);
    }
}
