package com.tfc.aitest;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Object {
	public ModelInstance mdl;
	public btCollisionObject collider;
	public boolean collision;
	public Vector3 veloc = new Vector3(0, 0, 0);
	public btCollisionShape shape;
	private static final ArrayList<Object> emptyArrayList = new ArrayList<>();
	
	public Object(ModelInstance mdl, btCollisionShape shape) {
		this.mdl = mdl;
		
		collider = new btCollisionObject();
		collider.setCollisionShape(shape);
		collider.setWorldTransform(mdl.transform);
		
		this.shape = shape;
	}
	
	public void update() {
		collider.setWorldTransform(mdl.transform);
	}
	
	public void dispose() {
		collider.dispose();
		shape.dispose();
	}
	
	public btManifoldResult getManifold(btCollisionObject o1, btCollisionObject o2, final QuadConsumer<btCollisionObjectWrapper, btCollisionObjectWrapper, btDispatcherInfo, btManifoldResult> consumer) {
		CollisionObjectWrapper co0 = new CollisionObjectWrapper(o1);
		CollisionObjectWrapper co1 = new CollisionObjectWrapper(o2);
		
		btCollisionAlgorithmConstructionInfo ci = new btCollisionAlgorithmConstructionInfo();
		ci.setDispatcher1(AITest.instance.dispatcher);
		btCollisionAlgorithm algorithm = new btSphereBoxCollisionAlgorithm(null, ci, co0.wrapper, co1.wrapper, false) {
			@Override
			public void processCollision(btCollisionObjectWrapper body0Wrap, btCollisionObjectWrapper body1Wrap, btDispatcherInfo dispatchInfo, btManifoldResult resultOut) {
				super.processCollision(body0Wrap, body1Wrap, dispatchInfo, resultOut);
				consumer.accept(body0Wrap, body1Wrap, dispatchInfo, resultOut);
			}
		};
		
		btDispatcherInfo info = new btDispatcherInfo();
		btManifoldResult result = new btManifoldResult(co0.wrapper, co1.wrapper);
		
		algorithm.processCollision(co0.wrapper, co1.wrapper, info, result);
		
		info.dispose();
		algorithm.dispose();
		ci.dispose();
		co1.dispose();
		co0.dispose();
		
		return result;
	}
	
	public void doBallCollision(ArrayList<Object> objects, ArrayList<btCollisionObject> collisionObjects) {
//		veloc.nor();
		if (veloc.y < -1) {
			veloc.y = -1;
		} else if (veloc.y > 1) {
			veloc.y = 1;
		}
		if (veloc.x < -1) {
			veloc.x = -1;
		} else if (veloc.x > 1) {
			veloc.x = 1;
		}
		if (veloc.z < -1) {
			veloc.z = -1;
		} else if (veloc.z > 1) {
			veloc.z = 1;
		}
		if (!collision) {
			mdl.transform.translate(veloc.cpy().scl(0.25f));
			update();
			veloc = veloc.add(0, -0.1f, 0);
			checkCollision(objects, collisionObjects);
		}
		if (collision) {
			for (Object object : objects) {
				btManifoldResult res = getManifold(collider, object.collider, (ball, ground, info, result) -> {
					for (int i = 0; i < result.getPersistentManifold().getNumContacts(); i++)
						handleContactBall(result.getPersistentManifold().getContactPoint(i), ground);
				});
				res.dispose();
			}
			for (btCollisionObject object : collisionObjects) {
				btManifoldResult res = getManifold(collider, object, (ball, ground, info, result) -> {
					for (int i = 0; i < result.getPersistentManifold().getNumContacts(); i++)
						handleContactBall(result.getPersistentManifold().getContactPoint(i), ground);
				});
				res.dispose();
			}
//			while (checkAndGetCollision(objects, collisionObjects)) {
//				mdl.transform.translate(0, 0.01f, 0);
//				update();
//				collision = false;
//			}
			checkAndGetCollision(objects,collisionObjects);
		}
		update();
	}
	
	private void handleContactBall(btManifoldPoint point, btCollisionObjectWrapper cause) {
		Vector3 pos = new Vector3();
		Vector3 pos2 = new Vector3();
		Vector3 pos1 = new Vector3();
		point.getLocalPointA(pos);
		point.getLocalPointA(pos2);
		point.getLocalPointB(pos1);
		pos = (pos.lerp(pos1, 0.2f)).nor().scl(-0.04f);
		ArrayList<btCollisionObject> causeList = new ArrayList<>();
		causeList.add(cause.getCollisionObject());
		int iterMax = 0;
		Vector3 off = pos2.scl(-0.001f);
		while (checkAndGetCollision(emptyArrayList,causeList)) {
			mdl.transform.translate(off);
			iterMax++;
			update();
			if (iterMax >= 100) {
				collision = true;
				mdl.transform.translate(off.scl(-0.01f));
				update();
				break;
			} else {
				collision = false;
			}
		}
		mdl.transform.translate(off.scl(-0.01f));
		update();
		veloc.add(pos);
	}
	
	public void checkCollision(ArrayList<Object> objects, ArrayList<btCollisionObject> collisionObjects) {
		AtomicBoolean r = new AtomicBoolean(false);
		for (Object object : objects) {
			if (!object.equals(this)) {
				btManifoldResult res = getManifold(collider, object.collider, (ball, ground, info, result) -> {
					if (result.getPersistentManifold().getNumContacts() > 0) {
						r.set(true);
					}
				});
				res.dispose();
			}
			if (r.get()) {
				collision = true;
				return;
			}
		}
		for (btCollisionObject object : collisionObjects) {
			btManifoldResult res = getManifold(collider, object, (ball, ground, info, result) -> {
				if (result.getPersistentManifold().getNumContacts() > 0) {
					r.set(true);
				}
			});
			res.dispose();
			if (r.get()) {
				collision = true;
				return;
			}
		}
		collision = false;
	}
	
	public boolean checkAndGetCollision(ArrayList<Object> objects, ArrayList<btCollisionObject> collisionObjects) {
		checkCollision(objects, collisionObjects);
		return collision;
	}
	
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Object object = o;
		return collision == object.collision &&
				Objects.equals(mdl, object.mdl) &&
				Objects.equals(collider, object.collider) &&
				Objects.equals(veloc, object.veloc);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(mdl, collider, collision, veloc);
	}
}
