package com.ac.springbootreactor;

import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        exampleFlatMap();
    }

    private void exampleFlatMap() {
        List<String> userList = new ArrayList<>();
        userList.add("Andres Ape");
        userList.add("Pedro Ape1");
        userList.add("Maria Ape1");
        userList.add("Diego Ape3");

        Flux.fromIterable(userList)
                .map(nombre -> {
                    return new User(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase());
                })
                .flatMap(user -> {
                    if (user.getLastName().equalsIgnoreCase("Ape1")) {
                        return Mono.just(user);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(user -> {
                    user.setName(user.getName().toLowerCase());
                    return user;
                })
                .subscribe(user -> log.info(user.toString()));
    }

    private void ExampleIterable() {

        List<String> userList = new ArrayList<>();
        userList.add("Andres Ape");
        userList.add("Pedro Ape1");
        userList.add("Maria Ape1");
        userList.add("Diego Ape3");

        Flux<String> nombres = Flux.fromIterable(userList);

//        Flux<String> nombres = Flux.just("Andres Ape", "Pedro Ape1", "Maria Ape1", "Diego Ape3");

        Flux<User> usuarios = nombres.map(nombre -> {
            return new User(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase());
        }).filter(user -> user.getLastName().toLowerCase().equals("ape1")).doOnNext(user -> {
            if (user == null) {
                throw new RuntimeException("Se encontro un nombre vacio");
            }
            System.out.println(user.getName());
        }).map(user -> {
            user.setName(user.getName().toLowerCase());
            return user;
        });

        usuarios.subscribe(user -> log.info(user.toString()), error -> log.error(error.getMessage()), new Runnable() {
            @Override
            public void run() {
                log.info("Ha finalizado la ejecución del observable con exito");
            }
        });
    }

}
