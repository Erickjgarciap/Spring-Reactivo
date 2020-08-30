package com.spring.reactive;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringReactiveApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringReactiveApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(SpringReactiveApplication.class, args);
	}
	/*
	 * 5517207000 79768 79776
	 * 
	 */

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub

		//ejemploFlatMap();
		//ejemploToString();
		//ejemploCollectList();
		//ejemploUsuarioComentariosFlatMap();
		//ejemploUsuarioComentariosZipWith2();
		//ejemploZipWithRango();
		//ejemploIntervalZip();
		//ejemploDelay();
		//ejemploIntervaloInfinito();
		ejemploContrapecion();
	}
	
	public void ejemploContrapecion() {
		Flux.range(1, 10)
		.log()
		.subscribe(new Subscriber<Integer>() {
			private Subscription s;
			private Integer limite =2;
			private Integer consumido = 0;
			@Override
			public void onSubscribe(Subscription s) {
				// TODO Auto-generated method stub
				this.s=s;
				s.request(limite);
				
				
			}

			@Override
			public void onNext(Integer t) {
				// TODO Auto-generated method stub
				log.info(t.toString());
				consumido++;
				if (consumido == limite) {
					consumido=0;
					s.request(limite);
				}
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}});
		
	}
	
	public void ejemploIntervaloInfinitoDesdeCreate() {
		
		Flux.create(emitter->{
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador=0;
				@Override
				public void run() {
					
					emitter.next(++contador);
					if (contador==10) {
						timer.cancel();
						emitter.complete();
					}
					if (contador==5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux"));
					}
				}				
			},1000,1000);
		})
		.subscribe(next ->log.info(next.toString()),
				   error -> log.error(error.getMessage()),
				   ()->log.info("Se ha terminado el flujo"));
	}
	public void ejemploIntervaloInfinito() throws InterruptedException{
		
		CountDownLatch latch = new CountDownLatch(1);
		
		
		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(latch::countDown)
		.flatMap(i-> {
			if (i>=5)
				return Flux.error(new InterruptedException("Solo hasta 5!"));
		
			return Flux.just(i);
			
		})
		.map(i-> "Hola " + i)
		.retry(2)
		.subscribe(s->log.info(s), e-> log.error(e.getMessage()));
		latch.await();
	}
	public void ejemploDelay() {
		Flux <Integer> rango = Flux.range(1, 12)
									.delayElements(Duration.ofSeconds(1))
									.doOnNext(i-> log.info(i.toString()));
		rango.subscribe();

	}
	
	public void ejemploIntervalZip() {
		Flux <Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		
		rango.zipWith(retraso, (ra,re)->ra)
		.doOnNext (i-> log.info(i.toString()))
		.subscribe();
	}
	
	
	public void ejemploZipWithRango() {
		
		Flux.just(1,2,3,4)
		.map(i -> (i*2))
		.zipWith(Flux.range(0, 4),(source,combinar)->{
			
			return String.format("Primer Flux: %d , Segundo Flux: %d",source,combinar);
		}).subscribe(texto -> log.info(texto));
		
		
	}
	
	public void ejemploUsuarioComentariosZipWith2() {
		  Mono <Usuario> usuarioMono  = Mono.fromCallable(()-> crearUsuario());
		  
		  Mono <Comentarios> comentarioUsuarioMono = Mono.fromCallable(()->{
			  
			  Comentarios comentarios = new Comentarios();
			  comentarios.addComentarios("Hola que tal!!!!");
			  comentarios.addComentarios("Mañana al trabajo no mms");
			  return comentarios;			  
		  });
		
		Mono<UsuarioComentarios> usuarioConComentarios = 
				 usuarioMono.zipWith(comentarioUsuarioMono)
				 .map(tuple ->{
					Usuario u = tuple.getT1(); //Datos del flujo 1 Usuario
					Comentarios c =tuple.getT2();// Datos dl flujo 2 Comentarios
					
					return new UsuarioComentarios(u,c);
				 });
		
		usuarioConComentarios.subscribe(usuarioComent -> log.info(usuarioComent.toString()));
		
		
		
	}
	public void ejemploUsuarioComentariosFlatMap() {
		  Mono <Usuario> usuarioMono  = Mono.fromCallable(()-> crearUsuario());
		  
		  Mono <Comentarios> comentarioUsuarioMono = Mono.fromCallable(()->{
			  
			  Comentarios comentarios = new Comentarios();
			  comentarios.addComentarios("Hola que tal!!!!");
			  comentarios.addComentarios("Mañana al trabajo no mms");
			  return comentarios;			  
		  });
		
		usuarioMono.flatMap(u ->comentarioUsuarioMono.map(coment -> new UsuarioComentarios(u,coment)))
		.subscribe(usuarioComent -> log.info(usuarioComent.toString()));
		
		
		
	}
	
	
	public Usuario crearUsuario() {
		
		return new Usuario ("Erick","Garcia");
	}
	public void ejemploUsuarioComentariosZipWith() {
		  Mono <Usuario> usuarioMono  = Mono.fromCallable(()-> crearUsuario());
		  
		  Mono <Comentarios> comentarioUsuarioMono = Mono.fromCallable(()->{
			  
			  Comentarios comentarios = new Comentarios();
			  comentarios.addComentarios("Hola que tal!!!!");
			  comentarios.addComentarios("Mañana al trabajo no mms");
			  return comentarios;			  
		  });
		
		Mono<UsuarioComentarios> usuarioConComentarios = 
				 usuarioMono.zipWith(comentarioUsuarioMono,(usuario,comentariosUsuario)-> 
											new UsuarioComentarios(usuario,comentariosUsuario));
		
		usuarioConComentarios.subscribe(usuarioComent -> log.info(usuarioComent.toString()));
		
		
		
	}
	
	public void ejemploCollectList() throws Exception {
		// TODO Auto-generated method stub
		List<Usuario> usuarios = new ArrayList<>();
		usuarios.add(new Usuario ("Alejandra","Lopez"));
		usuarios.add(new Usuario ("Erick","Garcia"));
		usuarios.add(new Usuario ("Elizandro","Cabañas"));
		usuarios.add(new Usuario ("Bruno","Ferras"));
		usuarios.add(new Usuario ("Maria","Trujillo"));
		usuarios.add(new Usuario ("Bruce","Lee"));
		usuarios.add(new Usuario ("Bruce","Willis"));

		Flux.fromIterable(usuarios)
		.collectList()
		.subscribe(listaUsuarios -> {
			
			listaUsuarios.forEach(item ->log.info(item.toString()));
		});

	}

	public void ejemploToString() throws Exception {
		// TODO Auto-generated method stub
		List<Usuario> usuarios = new ArrayList<>();
		usuarios.add(new Usuario ("Alejandra","Lopez"));
		usuarios.add(new Usuario ("Erick","Garcia"));
		usuarios.add(new Usuario ("Elizandro","Cabañas"));
		usuarios.add(new Usuario ("Bruno","Ferras"));
		usuarios.add(new Usuario ("Maria","Trujillo"));
		usuarios.add(new Usuario ("Bruce","Lee"));
		usuarios.add(new Usuario ("Bruce","Willis"));

		Flux.fromIterable(usuarios)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(usuario.getApellido().toString()))
				.flatMap(nombre -> {
					if (nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					}else {
						return Mono.empty();
					}
				}).map(nombre -> {
					return nombre.toLowerCase();
				})

				.subscribe(u -> log.info(u.toString()));

	}
	public void ejemploFlatMap() throws Exception {
		// TODO Auto-generated method stub
		List<String> usuarios = new ArrayList<>();
		usuarios.add("Alejandra Lopez");
		usuarios.add("Erick Garcia");
		usuarios.add("Elizandro Cabañas");
		usuarios.add("Bruno Ferras");
		usuarios.add("Maria Trujillo");
		usuarios.add("Bruce Lee");
		usuarios.add("Bruce Willis");

		Flux.fromIterable(usuarios)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("Bruce")) {
						return Mono.just(usuario);
					}else {
						return Mono.empty();
					}
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})

				.subscribe(u -> log.info(u.toString()));

	}

	public void ejemploIterable() throws Exception {
		// TODO Auto-generated method stub
		List<String> usuarios = new ArrayList<>();
		usuarios.add("Alejandra Lopez");
		usuarios.add("Erick Garcia");
		usuarios.add("Elizandro Cabañas");
		usuarios.add("Bruno Ferras");
		usuarios.add("Maria Trujillo");
		usuarios.add("Bruce Lee");
		usuarios.add("Bruce Willis");

		Flux<String> nombres = Flux
				.fromIterable(usuarios);/*
										 * Flux.just("Alejandra Lopez", "Erick Garcia", "Elizandro Cabañas",
										 * "Bruno Ferras", "Maria Trujillo", "Bruce Lee","Bruce Willis");
										 */

		Flux<Usuario> usua = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce")).doOnNext(usuario -> {

					if (null == usuario) {
						throw new RuntimeException("Nombre no pueden ser vacios");
					}
					log.info(usuario.getNombre().concat(" ").concat(usuario.getApellido()));

				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usua.subscribe(e -> log.info(e.getNombre().concat(" ").concat(e.getApellido())),
				err -> log.info(err.getMessage()), new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						log.info("Ha finalizado la ejecucion del observable con exito");
					}
				});

	}

}
