package com.ac.springbootreactor;

import models.Comments;
import models.User;
import models.UserComments;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.swing.undo.UndoableEditSupport;
import javax.xml.stream.events.Comment;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        backPressure();
    }

    private void backPressure() {
        Flux.range(1, 10)
                .log()
//                .limitRate(5) ejemplo reducido de backpressure
                .subscribe(new Subscriber<Integer>() { // manera mas personable de controlar las cargas

                    private Subscription s;
                    private Integer top = 5;
                    private Integer consumed = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(top);
                    }

                    @Override
                    public void onNext(Integer t) {
                        log.info(t.toString());
                        consumed++;
                        if (consumed.equals(top)) {
                            consumed = 0;
                            s.request(top);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void intervalCreate() {
        Flux.create(emitter -> {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        private Integer count = 0;

                        @Override
                        public void run() {
                            emitter.next(++count);
                            if (count == 10) {
                                timer.cancel();
                                emitter.complete();
                            }

                            if (count == 5) {
                                timer.cancel();
                                emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
                            }
                        }
                    }, 1000, 1000);
                })
                .subscribe(next -> log.info(next.toString()),
                        error -> log.error(error.getMessage()),
                        () -> log.info("Complete"));
    }

    private void intervalInfinite() throws InterruptedException {
        // count
        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown)
                .flatMap(number -> {
                    if (number >= 5) {
                        return Flux.error(new InterruptedException("Solo hasta 5!"));
                    }
                    return Flux.just(number);
                })
                .retry(1) // el retry es usado para reitentar el numero de veces si salta un problema.
                .map(number -> String.format("Soy %d", number))
                .subscribe(log::info, e -> log.error(e.getMessage()));

        latch.await();
    }

    private void exampleDelayInterval() {
        Flux<Integer> range = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(nummber -> log.info(nummber.toString()));

        range.blockLast();
    }

    private void exampleInterval() {
        Flux<Integer> range = Flux.range(1, 12);
        Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

        range.zipWith(delay, (ran, del) -> ran)
                .doOnNext(number -> log.info(number.toString()))
                .blockLast(); // bloquea o detiene el proceso first o last. No es recomendable usar por cuello de botella
    }

    private void operatorRange() {
        Flux<Integer> ranges = Flux.range(0, 4);
        Flux.just(1, 2, 3, 4)
                .map(n -> n * 2)
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
