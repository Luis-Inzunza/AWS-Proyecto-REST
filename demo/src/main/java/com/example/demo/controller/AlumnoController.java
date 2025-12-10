package com.example.demo.controller;

import com.example.demo.model.Alumnos;
import com.example.demo.repository.AlumnoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;
import java.security.SecureRandom;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private AmazonS3 s3Client;

    @org.springframework.beans.factory.annotation.Value("${aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private AmazonSNS snsClient;

    @Autowired
    private DynamoDB dynamoDB;

    
    @org.springframework.beans.factory.annotation.Value("${aws.sns.topic.arn}")
    private String topicArn;

    
    @GetMapping
    public List<Alumnos> obtenerAlumnos() {
        return alumnoRepository.findAll();
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<Alumnos> obtenerAlumnoPorId(@PathVariable int id) {
        Optional<Alumnos> alumno = alumnoRepository.findById(id);
        
        if (alumno.isPresent()) {
            return new ResponseEntity<>(alumno.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    
    @PostMapping
    public ResponseEntity<Alumnos> crearAlumno(@RequestBody Alumnos alumno) {
        if (!validarAlumno(alumno)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        Alumnos alumnoGuardado = alumnoRepository.save(alumno);
        return new ResponseEntity<>(alumnoGuardado, HttpStatus.CREATED);
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<Alumnos> actualizarAlumno(@PathVariable int id, @RequestBody Alumnos alumnoNuevo) {
        if (!validarAlumno(alumnoNuevo)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Alumnos> alumnoExistente = alumnoRepository.findById(id);

        if (alumnoExistente.isPresent()) {
            Alumnos alumno = alumnoExistente.get();
            alumno.setNombres(alumnoNuevo.getNombres());
            alumno.setApellidos(alumnoNuevo.getApellidos());
            alumno.setMatricula(alumnoNuevo.getMatricula());
            alumno.setPromedio(alumnoNuevo.getPromedio());
            if(alumnoNuevo.getPassword() != null && !alumnoNuevo.getPassword().isEmpty()){
                alumno.setPassword(alumnoNuevo.getPassword());
            }
            
            
            alumnoRepository.save(alumno);
            return new ResponseEntity<>(alumno, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminarAlumno(@PathVariable int id) {
        if (alumnoRepository.existsById(id)) {
            alumnoRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Subir Foto de Perfil del alumno
    @PostMapping("/{id}/fotoPerfil")
    public ResponseEntity<String> subirFotoPerfil(@PathVariable int id, @RequestParam("file") MultipartFile file) {
        Optional<Alumnos> alumnoOpt = alumnoRepository.findById(id);
        
        if (!alumnoOpt.isPresent()) {
            return new ResponseEntity<>("Alumno no encontrado", HttpStatus.NOT_FOUND);
        }

        try {
            
            File archivoConvertido = convertMultiPartToFile(file);
            
            
            String nombreArchivo = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            
           
            s3Client.putObject(new PutObjectRequest(bucketName, nombreArchivo, archivoConvertido)
                    .withCannedAcl(CannedAccessControlList.PublicRead)); 
            
            
            String urlFoto = s3Client.getUrl(bucketName, nombreArchivo).toString();
            
            
            Alumnos alumno = alumnoOpt.get();
            alumno.setFotoPerfilUrl(urlFoto);
            alumnoRepository.save(alumno);
            
            
            archivoConvertido.delete();

            return new ResponseEntity<>(urlFoto, HttpStatus.OK);

        } catch (IOException e) {
            return new ResponseEntity<>("Error al subir archivo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Calificaciones por Correo
    @PostMapping("/{id}/email")
    public ResponseEntity<String> enviarCorreoAlumno(@PathVariable int id) {
        Optional<Alumnos> alumnoOpt = alumnoRepository.findById(id);

        if (!alumnoOpt.isPresent()) {
            return new ResponseEntity<>("Alumno no encontrado", HttpStatus.NOT_FOUND);
        }

        Alumnos alumno = alumnoOpt.get();

        String mensaje = "Hola " + alumno.getNombres() + " " + alumno.getApellidos() + ",\n\n" +
                         "Tu promedio actual es: " + alumno.getPromedio() + "\n" +
                         "Matrícula: " + alumno.getMatricula() + "\n\n" +
                         "Saludos,\nSistema de Control Escolar AWS.";

        String asunto = "Reporte de Calificaciones - " + alumno.getMatricula();

        try {
            PublishRequest request = new PublishRequest(topicArn, mensaje, asunto);
            snsClient.publish(request);

            return new ResponseEntity<>("Correo enviado correctamente a través de SNS.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al enviar notificación: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Sesiones de alumnos 

    
    @PostMapping("/{id}/session/login")
    public ResponseEntity<Object> login(@PathVariable int id, @RequestBody Map<String, String> body) {
        String passwordInput = body.get("password");

        Optional<Alumnos> alumnoOpt = alumnoRepository.findById(id);
        if (!alumnoOpt.isPresent()) {
            return new ResponseEntity<>("Alumno no encontrado", HttpStatus.NOT_FOUND);
        }

        Alumnos alumno = alumnoOpt.get();

        // Verificar contraseña (comparación simple como pide el PDF)
        if (alumno.getPassword() != null && alumno.getPassword().equals(passwordInput)) {
            
            
            String sessionString = UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
            sessionString = sessionString.replace("-", ""); 
            long fechaActual = System.currentTimeMillis() / 1000L; 

            
            Table table = dynamoDB.getTable("sesiones-alumnos");
            try {
                Item item = new Item()
                        .withPrimaryKey("id", UUID.randomUUID().toString()) 
                        .withNumber("fecha", fechaActual)
                        .withNumber("alumnoId", id)
                        .withBoolean("active", true)
                        .withString("sessionString", sessionString);
                
                table.putItem(item);

                
                return new ResponseEntity<>(Map.of("sessionString", sessionString), HttpStatus.OK);

            } catch (Exception e) {
                return new ResponseEntity<>("Error en DynamoDB: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Contraseña incorrecta", HttpStatus.BAD_REQUEST);
        }
    }

    
    @PostMapping("/{id}/session/verify")
    public ResponseEntity<String> verifySession(@PathVariable int id, @RequestBody Map<String, String> body) {
        String sessionStringInput = body.get("sessionString");
        
        Table table = dynamoDB.getTable("sesiones-alumnos");

        try {
            
            ScanSpec scanSpec = new ScanSpec()
                    .withFilterExpression("sessionString = :s AND active = :a")
                    .withValueMap(new ValueMap().withString(":s", sessionStringInput).withBoolean(":a", true));

            var items = table.scan(scanSpec);
            
            
            if (items.iterator().hasNext()) {
                return new ResponseEntity<>("Sesión válida y activa", HttpStatus.OK); // 200 OK
            } else {
                return new ResponseEntity<>("Sesión inválida o inactiva", HttpStatus.BAD_REQUEST); // 400 Bad Request
            }

        } catch (Exception e) {
            return new ResponseEntity<>("Error verificando sesión", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/session/logout")
    public ResponseEntity<String> logout(@PathVariable int id, @RequestBody Map<String, String> body) {
        String sessionStringInput = body.get("sessionString");
        
        Table table = dynamoDB.getTable("sesiones-alumnos");

        try {
            ScanSpec scanSpec = new ScanSpec()
                    .withFilterExpression("sessionString = :s")
                    .withValueMap(new ValueMap().withString(":s", sessionStringInput));

            var items = table.scan(scanSpec);
            var iterator = items.iterator();

            if (iterator.hasNext()) {
                Item itemEncontrado = iterator.next();
                
                
                itemEncontrado.withBoolean("active", false);
                table.putItem(itemEncontrado); 

                return new ResponseEntity<>("Sesión cerrada exitosamente", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Sesión no encontrada", HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            return new ResponseEntity<>("Error al cerrar sesión", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private boolean validarAlumno(Alumnos alumno) {
        if (alumno.getNombres() == null || alumno.getNombres().isEmpty()) return false;
        if (alumno.getApellidos() == null || alumno.getApellidos().isEmpty()) return false;
        if (alumno.getMatricula() == null || alumno.getMatricula().isEmpty()) return false;
        if (alumno.getPromedio() < 0) return false;
        return true;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
