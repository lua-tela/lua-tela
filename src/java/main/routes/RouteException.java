package main.routes;

public class RouteException extends Exception
{
    RouteException(String message)
    {
        super(message);
    }

    RouteException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
