package com.ac.springbootreactor;

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

		Flux<String> nombres = Flux.just("Andres", "Pedro", "Maria", "Diego").doOnNext(nombre -> {
			if (nombre.isEmpty()) {
				throw new RuntimeException("Se encontro un nombre vacio");
			}
			System.out.println(nombre);
		});

		// explesion lambda
//		Flux<String> nombres = Flux.just("Andres", "Pedro", "Juan", "Diego").doOnNext(System.out::println);

		nombres.subscribe(e -> log.info(e),
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
