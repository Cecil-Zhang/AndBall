package com.nju.andball;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;

public class StartActivity extends BaseGameActivity {
	// 摄像头尺寸  
	private static final int CAMERA_WIDTH = 800;  
	private static final int CAMERA_HEIGHT = 480;  
	private Camera mCamera;
	// 贴图  
	private Texture mTexture;  
	private TextureRegion mLeekaoTextureRegion;
	
	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public Engine onLoadEngine() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);  
		  
		// TODO Auto-generated method stub  
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,  
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),  
		this.mCamera));
	}

	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub
		this.mTexture = new Texture(1024, 1024,  TextureOptions.BILINEAR_PREMULTIPLYALPHA);  
		this.mLeekaoTextureRegion = TextureRegionFactory.createFromAsset(  
				this.mTexture, this, "leekao/leekao.jpg", 0, 0);  
		this.mEngine.getTextureManager().loadTexture(this.mTexture); 
	}

	@Override
	public Scene onLoadScene() {
		// TODO Auto-generated method stub
		this.mEngine.registerUpdateHandler(new FPSLogger());  
		final Scene scene=new Scene(1);  
		//摄像头置于画面中央  
		final int centerX=(CAMERA_WIDTH-this.mLeekaoTextureRegion.getWidth())/2;  
		final int centerY=(CAMERA_HEIGHT-this.mLeekaoTextureRegion.getHeight())/2;  
		  
		//创建Sprite对象并加入Scene对象中  
		final Sprite leekao=new Sprite(centerX,centerY, mLeekaoTextureRegion);  
		scene.getLastChild().attachChild(leekao);  
		return scene;
	}

}
