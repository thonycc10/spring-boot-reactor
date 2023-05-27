package com.ac.springbootreactor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) {

		/*Flux<String> nombres = Flux.just("Andres", "Pedro", "Juan", "Diego").doOnNext(nombre -> {
			System.out.println(nombre);
		});*/

		// explesion lambda
		Flux<String> nombres = Flux.just("Andres", "Pedro", "Juan", "Diego").doOnNext(System.out::println);

		nombres.subscribe();
	}
}
