package clientserver;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import java.util.List;

public interface Hello extends Remote {
    int add(int a, int b) throws RemoteException;
    int[] sort(int[] arr) throws RemoteException;
    public void uploadAuto() throws IOException;


    CompletableFuture<Integer> asyncAdd(int a, int b) throws RemoteException;
    CompletableFuture<List<Integer>> asyncSort(int[] arr) throws RemoteException;
    // CompletableFuture<List<Integer>> asyncSort(int[] arr) throws RemoteException;
}