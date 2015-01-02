package gui;

public interface IView {
	public void updateStatus(String text);
	public void setErrorMessage(String text);
	public void showSuccessMessage(String text);
}
