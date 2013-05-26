package models;

/**
 * Created with IntelliJ IDEA.
 * User: jimhao
 * Date: 07/05/2013
 * Time: 19:33
 * To change this template use File | Settings | File Templates.
 */

import awsClient4.*;

import javax.xml.ws.Holder;
import java.math.BigInteger;
import java.util.List;

public class TestClient{

    private static final String AWS_ACCESS_KEY_ID = "AKIAI5E6DRO2F3OMZNEA";
    private static final String AWS_SECRET_KEY = "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";
    private static final String ASSOCIATE_TAG = "buyforyou01-20";

//    public static void main(String[] args) {
//        TestClient ist = new TestClient();
//        ist.runSearch();
//    }

    public List<Item> runSearch(final String searchTerm, final String searchIndex)
    {
        AWSECommerceService service = new AWSECommerceService();
        service.setHandlerResolver(new AwsHandlerResolver(AWS_SECRET_KEY));
        AWSECommerceServicePortType port = service.getAWSECommerceServicePort();

        ItemSearch ItemSearch = new ItemSearch();
        ItemSearch.setAWSAccessKeyId(AWS_ACCESS_KEY_ID);
        ItemSearch.setAssociateTag(ASSOCIATE_TAG);

        ItemSearchRequest SearchRequest = new ItemSearchRequest();
        java.util.List<ItemSearchRequest> list = ItemSearch.getRequest();
        list.add(SearchRequest);

        SearchRequest.setSearchIndex(searchIndex);
        SearchRequest.setKeywords(searchTerm);

        ItemSearch.getRequest().add(SearchRequest);

        java.util.List<String> responseGroup = SearchRequest.getResponseGroup();
        responseGroup.add("Images");
        responseGroup.add("ItemAttributes");
        responseGroup.add("Large");

        Holder<OperationRequest> operationrequest = new Holder<OperationRequest>();
        Holder<java.util.List<Items>> items = new Holder<java.util.List<Items>>();

        port.itemSearch(
                ItemSearch.getMarketplaceDomain(),
                ItemSearch.getAWSAccessKeyId(),
                ItemSearch.getAssociateTag(),
                ItemSearch.getXMLEscaping(),
                ItemSearch.getValidate(),
                ItemSearch.getShared(),
                ItemSearch.getRequest(),
                operationrequest,
                items);

        java.util.List<Items> result = items.value;
        BigInteger totalPages = result.get(0).getTotalResults();
        System.out.println(totalPages);

        for (int i = 0; i < result.get(0).getItem().size(); ++i)
        {	Item myItem = result.get(0).getItem().get(i);
            System.out.print(myItem.getASIN());
            System.out.print(", ");
            System.out.println(myItem.getDetailPageURL());
            System.out.print(", ");
            System.out.println(myItem.getSmallImage() == null ? "" : myItem.getSmallImage().getURL());
        }
        return result.get(0).getItem();
    }


    private final static String asinDefault = "0385349947";
    public String addToCart(final String asin, final int qty)
    {
        AWSECommerceService service = new AWSECommerceService();
        service.setHandlerResolver(new AwsHandlerResolver(AWS_SECRET_KEY));
        AWSECommerceServicePortType port = service.getAWSECommerceServicePort();

        CartCreate cartCreate = new CartCreate();
        cartCreate.setAWSAccessKeyId(AWS_ACCESS_KEY_ID);
        cartCreate.setAssociateTag(ASSOCIATE_TAG);

        CartCreateRequest cartCreateRequest = new CartCreateRequest();
        java.util.List<CartCreateRequest> list = cartCreate.getRequest();
        list.add(cartCreateRequest);

        CartCreateRequest.Items.Item cartItem = new CartCreateRequest.Items.Item();
        cartItem.setASIN(asin);
        cartItem.setAssociateTag(ASSOCIATE_TAG);
        cartItem.setQuantity(new BigInteger(qty+""));
        CartCreateRequest.Items cartItems = new CartCreateRequest.Items();
        cartItems.getItem().add(cartItem);
        cartCreateRequest.setItems(cartItems);

        cartCreate.getRequest().add(cartCreateRequest);

        Holder<OperationRequest> operationrequest = new Holder<OperationRequest>();
        Holder<java.util.List<Cart>> cart = new Holder<java.util.List<Cart>>();

        port.cartCreate(
                cartCreate.getMarketplaceDomain(),
                cartCreate.getAWSAccessKeyId(),
                cartCreate.getAssociateTag(),
                cartCreate.getXMLEscaping(),
                cartCreate.getValidate(),
                cartCreate.getShared(),
                cartCreate.getRequest(),
                operationrequest,
                cart);
        java.util.List<Cart> result = cart.value;
        Price price = result.get(0).getSubTotal();
        System.out.println(price);

        return result.get(0).getPurchaseURL();
    }
}