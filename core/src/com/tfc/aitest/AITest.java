package com.tfc.aitest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AITest extends ApplicationAdapter {
	public static AITest instance;
	
	private static final ArrayList<Object> emptyArrayList = new ArrayList<>();
	
	PerspectiveCamera cam;
	CameraInputController camController;
	ModelBatch batch;
	ArrayList<ModelInstance> instances;
	Environment environment;
	Model model;
	btCollisionConfiguration collisionConfig;
	public btDispatcher dispatcher;
	
	ArrayList<Object> allObjects = new ArrayList<>();
	ArrayList<Object> allObjects2 = new ArrayList<>();
	ArrayList<btCollisionObject> collisionObjects = new ArrayList<>();
	
	@Override
	public void create () {
		instance = this;
		Bullet.init();
		batch = new ModelBatch();
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(25f, 0f, 0f);
		cam.lookAt(0, 0, 0);
		cam.update();
		
		camController = new CameraInputController(cam) {
			@Override
			public boolean scrolled(int amount) {
				cam.fieldOfView += amount * 5f;
				return true;
			}
		};
		Gdx.input.setInputProcessor(camController);
		
		ModelBuilder mb = new ModelBuilder();
		mb.begin();
		mb.node().id = "ground";
		mb.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
				.box(5f, 1f, 5f);
		mb.node().id = "ball";
		mb.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.GREEN)))
				.sphere(1f, 1f, 1f, 10, 10);
		model = mb.end();
		
		instances = new ArrayList<ModelInstance>();
		
		for (int i = 0; i < 100; i++) {
			Object ball;
			ball = new Object(new ModelInstance(model, "ball"), new btSphereShape(0.5f));
			ball.mdl.transform.setToTranslation(0, 0, 0);
			ball.mdl.transform.translate(0, 0, 0f);
			instances.add(ball.mdl);
			allObjects.add(ball);
		}
		
		collisionConfig = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfig);
		
		float yBottom = -4*2.5f-1.4f;
		Object ground = new Object(new ModelInstance(model, "ground"), new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)));
		ground.mdl.transform.translate(0,yBottom,0);
		ground.mdl.transform.rotate(1, 0, 0, 45);
		ground.update();
		instances.add(ground.mdl);
		collisionObjects.add(ground.collider);
		allObjects2.add(ground);
		
		ground = new Object(new ModelInstance(model, "ground"), new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)));
		ground.mdl.transform.translate(0, yBottom, 2.825f);
		ground.mdl.transform.rotate(1, 0, 0, -45);
		ground.update();
		instances.add(ground.mdl);
		collisionObjects.add(ground.collider);
		allObjects2.add(ground);
		
		ground = new Object(new ModelInstance(model, "ground"), new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)));
		ground.mdl.transform.translate(0,-yBottom,0);
		ground.mdl.transform.rotate(1, 0, 0, -45);
		ground.update();
		instances.add(ground.mdl);
		collisionObjects.add(ground.collider);
		allObjects2.add(ground);
		
		ground = new Object(new ModelInstance(model, "ground"), new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)));
		ground.mdl.transform.translate(0, -yBottom, 2.825f);
		ground.mdl.transform.rotate(1, 0, 0, 45);
		ground.update();
		instances.add(ground.mdl);
		collisionObjects.add(ground.collider);
		allObjects2.add(ground);
		
		for (int y = -3; y < 4; y++) {
			ground = new Object(new ModelInstance(model, "ground"), new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)));
			ground.mdl.transform.translate(0, (y*2.5f), 4.45f);
			ground.mdl.transform.rotate(1, 0, 0, -90);
			ground.update();
			instances.add(ground.mdl);
			collisionObjects.add(ground.collider);
			allObjects2.add(ground);
			
			ground = new Object(new ModelInstance(model, "ground"), new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f)));
			ground.mdl.transform.translate(0, (y*2.5f), -1.62f);
			ground.mdl.transform.rotate(1, 0, 0, -90);
			ground.update();
			instances.add(ground.mdl);
			collisionObjects.add(ground.collider);
			allObjects2.add(ground);
		}
	}

	@Override
	public void render () {
		camController.update();

//		cam.position.set(allObjects.get(0).mdl.transform.getTranslation(new Vector3()));
//		cam.position.add(cam.direction.cpy().scl(-10));
		cam.direction.nor();
		cam.update();
		
		for (Object o : allObjects) o.doBallCollision(allObjects,collisionObjects);
		
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		batch.begin(cam);
		batch.render(instances, environment);
		batch.end();
		
		try {
//			Thread.sleep(100);
		} catch (Throwable ignored) {
		}
	}
	
	@Override
	public void dispose () {
		for (Object o : allObjects) o.dispose();
		for (Object o : allObjects2) o.dispose();
		
		dispatcher.dispose();
		collisionConfig.dispose();
		
		batch.dispose();
		model.dispose();
	}
}
