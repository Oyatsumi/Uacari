package filters.blur;

public class ShakeEffect extends GaussianBlur{
	
	public ShakeEffect(){
		this.setAmplitude(2);
	}
	public ShakeEffect(final float amplitude){
		this.setAmplitude(amplitude);
	}

	@Override
	public void setAmplitude(final float amplitude){
		this.amplitude = 1f/Math.abs(amplitude);
		this.update = true;
	}
	
}
