package fans.skin.expresstrade.models;

/** ResponseModel Model */

public class ResponseModel<T> {
    public T response;
    public Integer status;
    public Integer current_page;
    public Integer total_pages;
    public String message;
}
