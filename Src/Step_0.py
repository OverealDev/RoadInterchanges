#Step 0
#Creating points, then connect them using portion of circles.

import numpy as np
from matplotlib import pyplot as plt
from matplotlib import patches
import math
import random as rd
import scipy.stats


def circleTreePoints() :
    """ This function draw a circle that goes throught 3 points whose coordinates are given by x and y"""

    #Random sample of points
    x = np.array([3,5,6])
    y = np.array([3,5,12])

    [x1,x2,x3] = x
    [y1,y2,y3] = y

    """ The equation of the cirle is given by the following equation :

    det([[x**2,y**2,x,y,1],
         [x1**2,y1**2,x1,y1,1],
         [x2**2,y2**2,x2,y2,1],
         [x3**2,y3**2,x3,y3,1]]) = 0

    which is tantamount to the equation :

    a(x**2 + y**2) + b*x + c*y + d = 0
         """
    a = x1*y2 - x1*y3 - y1*x2 + y1*x3 + x2*y3 - y2*x3

    b = (x1**2)*y3 + (y1**2)*y3 - y2*(x1**2) - (y1**2)*y2 + y1*(x2**2) + y1*(y2**2) - y1*(x3**2) - y1*(y3**2) - y3*(x2**2) - y3*(y2**2) + (x3**2)*y2 + y2*(y3**2)

    c = - (x1**2)*x3 - (y1**2)*x3 + x2*(x1**2) + (y1**2)*x2 - x1*(x2**2) - x1*(y2**2) + x1*(x3**2) + x1*(y3**2) + x3*(x2**2) + x3*(y2**2) - (x3**2)*x2 - x2*(y3**2)

    d = y2*x3*(x1**2 + y1**2) - x2*y3*(x1**2 + y1**2) + x1*y3*(x2**2 + y2**2) - x1*y2*(x3**2 + y3**2) + y1*x2*(x3**2 + y3**2) - y1*x3*(x2**2 + y2**2)

    # (xc,yc) : coordinate of the center of the circle
    xc = -b/(2*a)
    yc = -c/(2*a)

    # RR : the square of the radius
    RR = (b**2 + c**2)/((2*a)**2) - (d/a)
    R = math.sqrt(RR)


    circle = plt.Circle((xc,yc),R,fill=False)

    """considering z<i> whose affix is the point <i> i.e. (x<i>,y<i>). theta<i> is the principal complex angle of z<i> considering the affine space centered on the affix of (xc + i*yc) """


    if (x1-xc) >= 0 :
        theta1 = math.atan((y1-yc)/(x1-xc))
    else :
        theta1 = math.atan((y1-yc)/(x1-xc)) + math.pi

    if (x3-xc) >= 0 :
        theta2 = math.atan((y3-yc)/(x3-xc))
    else :
        theta2 = math.atan((y3-yc)/(x3-xc)) + math.pi

    arc_circle = patches.Arc((xc,yc),2*R,2*R,0,theta1*180/math.pi,theta2*180/math.pi, color = "red",linewidth = 3)

    """gca : get the current axis"""
    ax = plt.gca()
    ax.scatter(x,y)
    ax.set_aspect('equal', 'box')
    ax.add_patch(circle)
    ax.add_patch(arc_circle)
    ax.set_title("drawing a circle that goes through 3 points")
    ax.set_xlabel("x")
    ax.set_ylabel("y")



    plt.grid()
    plt.show()


def circleMultiplePoints(points) :

    """ points : a list. each element (point) is a couple (x,y) that represents the coordinate of the point.

    This function displays points, and draws circles that goes throught them, three by three.

    Example : circleMultiplePoints(pointsGenerator(0.5,10,50,lambda x : math.cos(x) + math.sin(x)**2,0.2, False))"""

    xs = []
    ys = []
    for point in points :
        xs.append(point[0])
        ys.append(point[1])

    """ A list. LinkPointsCirlces[i] is a list that contains the index of circles that goes throught the point points[i] """
    LinkPointsCircles = [[] for k in range(len(points))]



    Circles = []
    Arc_circles = []

    i = 0

    while i <= len(points)-3 :
        [x1,x2,x3] = [xs[i],xs[i+1],xs[i+2]]
        [y1,y2,y3] = [ys[i],ys[i+1],ys[i+2]]
        Circles.append(calculateCircleAndArc(x1,x2,x3,y1,y2,y3)[0])
        Arc_circles.append(calculateCircleAndArc(x1,x2,x3,y1,y2,y3)[1])


        LinkPointsCircles[i].append(i//2)
        LinkPointsCircles[i+1].append(i//2)
        LinkPointsCircles[i+2].append(i//2)

        i +=2 #Circles have at least one point that is "owned" by another circle





    X = []
    Y = []
    for i in range(len(points)) :
        for circleIndex in LinkPointsCircles[i] :
            X.append(i)
            Y.append(1/(Circles[circleIndex].get_radius()))


    figure, axis = plt.subplots(1,2)
    axis[0].scatter(xs,ys)
    axis[0].autoscale()
    axis[0].set_aspect('equal', 'box')
    axis[0].set_title("Arc of circles passing through points")

    for circle in Circles :
        axis[0].add_patch(circle)

    for arc_circle in Arc_circles :
        axis[0].add_patch(arc_circle)


    axis[1].plot(X,Y)
    axis[1].set_title("Curvature of circles")
    axis[1].set_xlabel("number of the points")
    axis[1].set_ylabel("(1/Radius)")

    plt.grid()
    plt.show()




def circleMultiplePointsReturn(points) :

    """ points : a list. each element (point) is a couple (x,y) that represents the coordinate of the point.

    This function return arc of circles ,  that goes throught them points, three by three."""

    xs = []
    ys = []
    for point in points :
        xs.append(point[0])
        ys.append(point[1])

    """ A list. LinkPointsCirlces[i] is a list that contains the index of circles that goes throught the point points[i] """
    LinkPointsCircles = [[] for k in range(len(points))]



    Circles = []
    Arc_circles = []

    i = 0

    while i <= len(points)-3 :
        [x1,x2,x3] = [xs[i],xs[i+1],xs[i+2]]
        [y1,y2,y3] = [ys[i],ys[i+1],ys[i+2]]
        Circles.append(calculateCircleAndArc(x1,x2,x3,y1,y2,y3)[0])
        Arc_circles.append(calculateCircleAndArc(x1,x2,x3,y1,y2,y3)[1])


        LinkPointsCircles[i].append(i//2)
        LinkPointsCircles[i+1].append(i//2)
        LinkPointsCircles[i+2].append(i//2)

        i +=2 #Circles have at least one point that is "owned" by another circle

    return [Circles, Arc_circles]





def calculateCircleAndArc(x1,x2,x3,y1,y2,y3) :
    """ return : a circle that goes throught the 3 points (x1,y1), (x2,y2) and (x3,y3)"""

    a = x1*y2 - x1*y3 - y1*x2 + y1*x3 + x2*y3 - y2*x3

    b = (x1**2)*y3 + (y1**2)*y3 - y2*(x1**2) - (y1**2)*y2 + y1*(x2**2) + y1*(y2**2) - y1*(x3**2) - y1*(y3**2) - y3*(x2**2) - y3*(y2**2) + (x3**2)*y2 + y2*(y3**2)

    c = - (x1**2)*x3 - (y1**2)*x3 + x2*(x1**2) + (y1**2)*x2 - x1*(x2**2) - x1*(y2**2) + x1*(x3**2) + x1*(y3**2) + x3*(x2**2) + x3*(y2**2) - (x3**2)*x2 - x2*(y3**2)

    d = y2*x3*(x1**2 + y1**2) - x2*y3*(x1**2 + y1**2) + x1*y3*(x2**2 + y2**2) - x1*y2*(x3**2 + y3**2) + y1*x2*(x3**2 + y3**2) - y1*x3*(x2**2 + y2**2)

    xc = -b/(2*a)
    yc = -c/(2*a)
    RR = (b**2 + c**2)/((2*a)**2) - (d/a)
    R = math.sqrt(RR)

    circle = plt.Circle((xc,yc),R,fill=False,alpha = 0.3)



    """considering z<i> whose affix is the point <i> i.e. (x<i>,y<i>). theta<i> is the principal complex angle of z<i> considering the affine space centered on the affix of (xc + i*yc) """

    if (x1-xc) >= 0 :
        theta1 = math.atan((y1-yc)/(x1-xc))
    else :
        theta1 = math.atan((y1-yc)/(x1-xc)) + math.pi

    if (x2-xc) >= 0 :
        theta2 = math.atan((y2-yc)/(x2-xc))
    else :
        theta2 = math.atan((y2-yc)/(x2-xc)) + math.pi

    if (x3-xc) >= 0 :
        theta3 = math.atan((y3-yc)/(x3-xc))
    else :
        theta3 = math.atan((y3-yc)/(x3-xc)) + math.pi


    """The arc of circle that connect the point i and i+2 must go through the point i+1"""
    if theta3 < theta2 and theta2 < theta1 :
        (theta1,theta3) = (theta3, theta1)


    arc_circle = patches.Arc((xc,yc),2*R,2*R,0,theta1*180/math.pi,theta3*180/math.pi, color = "red",linewidth = 3)


    return [circle, arc_circle]



def pointsGenerator(xMin,xMax,numberOfPoints,function,noise,show) :
    """ Return : a list of points. A point is a list [x,y], where x and y represents the coordinates

    example : pointsGenerator(-4,7,30,lambda x : math.sin(x),0.4, True)"""


    xs = np.linspace(xMin,xMax,numberOfPoints)
    f = function
    ys = [f(x) for x in xs]
    res = []
    #adding noise
    for i in range(len(ys)) :
        ys[i] = ys[i] + (2*rd.random()-1)*(noise)

    for i in range(len(ys)) :
        res.append([xs[i],ys[i]])

    if show :
        plt.scatter(xs,ys)
        plt.show()
    return res


def regressionLines(points, thresholdEnd,limrvalue,thresholdCurve) :
    threshold_init = thresholdEnd
    """
    idea to detect straight lines : I take the first point, and the second one. While the line regression between the first and the second point is good enough, I take the next point for the "second point". if the while is "too short", then it wasn't a line, but a curve. Then the "last point" become the "first", we we do it again until the very last point.

    example : regressionLines(pointsGenerator(0.5,10,50,lambda x : math.cos(x) + math.sin(x)**2,0.1, False),3,0.95,5)
    """

    xs = []
    ys = []
    for point in points :
        xs.append(point[0])
        ys.append(point[1])

    thresholdStart = 0
    res_reg = []


    xs_init = xs[thresholdStart:thresholdEnd]
    ys_init = ys[thresholdStart:thresholdEnd]
    xs_init = np.array(xs_init)

    res = scipy.stats.linregress(xs_init, ys_init)


    while thresholdEnd < len(xs) :

        while ((res.rvalue**2 > limrvalue) and (thresholdEnd < len(xs))):



            print("thresholdStart : " + str(thresholdStart))
            print("thresholdEnd : " + str(thresholdEnd))



            xs_init = xs[thresholdStart:thresholdEnd]
            ys_init = ys[thresholdStart:thresholdEnd]
            res = scipy.stats.linregress(xs_init, ys_init)
            thresholdEnd += 1
            print("rvalue**2 : " + str(res.rvalue**2)+ "\n")


        if (thresholdEnd - thresholdStart) >= thresholdCurve :
            xs_init = np.array(xs_init)
            plt.plot(xs_init, res.intercept + res.slope*xs_init, 'r', label='fitted line')
        else :

            print("courbe!")
            print("thresholdStart : " + str(thresholdStart))
            print("thresholdEnd : " + str(thresholdEnd) + "\n")

            pointsCurve = []
            k = thresholdStart
            while k < thresholdEnd :
                pointsCurve.append([xs[k],ys[k]])
                k+=1

            Circles = circleMultiplePointsReturn(pointsCurve)[0]
            Arc_circles = circleMultiplePointsReturn(pointsCurve)[1]
            axis = plt.gca()
            for circle in Circles :
                axis.add_patch(circle)

            for arc_circle in Arc_circles :
                axis.add_patch(arc_circle)







        thresholdStart = thresholdEnd-1
        thresholdEnd += threshold_init


        xs_init = xs[thresholdStart:thresholdEnd]
        ys_init = ys[thresholdStart:thresholdEnd]
        res = scipy.stats.linregress(xs_init, ys_init)



    plt.title("Algorithm trying to find straight lines")
    axis.set_aspect('equal', 'box')
    plt.legend()
    plt.scatter(xs,ys,alpha=0.4)
    plt.show()


def roadConstructor(show) :

    R = 0.5
    xs = [1,4,8,12,17]
    ys = [1,4,3,5,4]
    ysbis = []
    for y in ys :
        ysbis.append(y-R)
    res = []


    for i in range(len(ys)) :
        res.append([xs[i],ys[i]])

    if show :
        plt.scatter(xs,ys)
        plt.plot(xs,ys, alpha = 0.3)
        plt.scatter(xs,ysbis)
        plt.plot(xs,ysbis)
        axis = plt.gca()
        axis.set_aspect('equal', 'box')
        axis = plt.gca()


    for points in res :
        xc = points[0]
        yc = points[1]-R
        circle = plt.Circle((xc,yc),R,fill=False,alpha = 0.3)
        axis.add_patch(circle)

    plt.show()



    return res
