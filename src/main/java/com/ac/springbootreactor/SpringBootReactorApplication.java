package com.ac.springbootreactor;

import models.Comments;
import models.User;
import models.UserComments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.swing.undo.UndoableEditSupport;
import javax.xml.stream.events.Comment;
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
        operatorRange();
    }

    private void operatorRange() {
        Flux<Integer> ranges = Flux.range(0,4);
        Flux.just(1,2,3,4)
                .map(n -> n*2)
                .zipWith(ranges, (map, range) -> String.format("Primer Flux: %d, Segundo Flux: %d", map, range))
                .subscribe(texto -> log.info(texto));
    }

    //    Combina dos flujos
    private void userCommentsZipWith2() {
        Mono<User> userMono = Mono.fromCallable(() -> new User("Thony", "Carrasco"));

        Mono<Comments> commentMono = Mono.fromCallable(() -> {
            Comments comments = new Comments();
            comments.addComment("Hola thony");
            comments.addComment("como estas thony");
            comments.addComment("que te cuentas thony");
            comments.addComment("vamos papi tu puedes!");
            return comments;
        });

        Mono<UserComments> userCommentsMono = userMono
                .zipWith(commentMono)
                .map(tuple -> {
                   User u = tuple.getT1();
                   Comments c = tuple.getT2();
                   return new UserComments(u, c);
                });

        userCommentsMono.subscribe(userComment -> log.info(userComment.toString()));
    }

    private void userCommentsZipWith() {
        Mono<User> userMono = Mono.fromCallable(() -> new User("Thony", "Carrasco"));

        Mono<Comments> commentMono = Mono.fromCallable(() -> {
            Comments comments = new Comments();
            comments.addComment("Hola thony");
            comments.addComment("como estas thony");
            comments.addComment("que te cuentas thony");
            comments.addComment("vamos papi tu puedes!");
            return comments;
        });

        Mono<UserComments> userCommentsMono = userMono.zipWith(commentMono, (user, commets) -> new UserComments(user, commets));

        userCommentsMono.subscribe(userComment -> log.info(userComment.toString()));
    }

    private void userCommentsFlatMap() {
        Mono<User> userMono = Mono.fromCallable(() -> new User("Thony", "Carrasco"));

        Mono<Comments> commentMono = Mono.fromCallable(() -> {
            Comments comments = new Comments();
            comments.addComment("Hola thony");
            comments.addComment("como estas thony");
            comments.addComment("que te cuentas thony");
            comments.addComment("vamos papi tu puedes!");
            return comments;
        });

        userMono.flatMap(user -> commentMono.map(commet -> new UserComments(user, commet)))
                .subscribe(userComment -> log.info(userComment.toString()));
    }

    private void convertObservableFluxToMono() {
        List<User> userList = new ArrayList<>();
        userList.add(new User("Andres", "Ape1"));
        userList.add(new User("Pepe", "Ape3"));
        userList.add(new User("Juan", "Ape2"));
        userList.add(new User("Diego", "Ape1"));

        Flux.fromIterable(userList)
                .collectList()
                .subscribe(list -> {
                    list.forEach(user -> log.info(user.toString()));
                });
    }

    private void exampleToString() {
        List<User> userList = new ArrayList<>();
        userList.add(new User("Andres", "Ape1"));
        userList.add(new User("Pepe", "Ape3"));
        userList.add(new User("Juan", "Ape2"));
        userList.add(new User("Diego", "Ape1"));

        Flux.fromIterable(userList)
                .map(user -> user.getName().toUpperCase().concat(" ").concat(user.getLastName().toUpperCase()))
                .flatMap(name -> {
                    if (name.contains("Ape1".toUpperCase())) {
                        return Mono.just(name);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(String::toLowerCase)
                .subscribe(user -> log.info(user.toString()));
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
