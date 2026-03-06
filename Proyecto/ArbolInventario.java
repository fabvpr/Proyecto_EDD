package Proyecto;

import java.util.List;

/**
 * ArbolInventario - Gestión de inventario jerárquico con árbol binario
 */
public class ArbolInventario {
    private Nodo raiz;

    private class Nodo {
        Producto producto;
        Nodo izquierdo, derecho;

        Nodo(Producto p) {
            producto = p;
            izquierdo = derecho = null;
        }
    }

    public void insertar(Producto p) {
        raiz = insertarRec(raiz, p);
    }

    private Nodo insertarRec(Nodo raiz, Producto p) {
        if (raiz == null) {
            raiz = new Nodo(p);
            return raiz;
        }
        if (p.getSku() < raiz.producto.getSku())
            raiz.izquierdo = insertarRec(raiz.izquierdo, p);
        else if (p.getSku() > raiz.producto.getSku())
            raiz.derecho = insertarRec(raiz.derecho, p);
        return raiz;
    }

    public Producto buscar(int sku) {
        return buscarRec(raiz, sku);
    }

    private Producto buscarRec(Nodo raiz, int sku) {
        if (raiz == null || raiz.producto.getSku() == sku)
            return raiz != null ? raiz.producto : null;
        if (sku < raiz.producto.getSku())
            return buscarRec(raiz.izquierdo, sku);
        return buscarRec(raiz.derecho, sku);
    }

    public void eliminar(int sku) {
        raiz = eliminarRec(raiz, sku);
    }

    private Nodo eliminarRec(Nodo raiz, int sku) {
        if (raiz == null) return raiz;
        if (sku < raiz.producto.getSku())
            raiz.izquierdo = eliminarRec(raiz.izquierdo, sku);
        else if (sku > raiz.producto.getSku())
            raiz.derecho = eliminarRec(raiz.derecho, sku);
        else {
            if (raiz.izquierdo == null) return raiz.derecho;
            else if (raiz.derecho == null) return raiz.izquierdo;
            raiz.producto = minValor(raiz.derecho);
            raiz.derecho = eliminarRec(raiz.derecho, raiz.producto.getSku());
        }
        return raiz;
    }

    private Producto minValor(Nodo raiz) {
        Producto minv = raiz.producto;
        while (raiz.izquierdo != null) {
            minv = raiz.izquierdo.producto;
            raiz = raiz.izquierdo;
        }
        return minv;
    }

    public void inorder(List<Producto> lista) {
        inorderRec(raiz, lista);
    }

    private void inorderRec(Nodo raiz, List<Producto> lista) {
        if (raiz != null) {
            inorderRec(raiz.izquierdo, lista);
            lista.add(raiz.producto);
            inorderRec(raiz.derecho, lista);
        }
    }

    public Nodo getRaiz() { return raiz; }
}
