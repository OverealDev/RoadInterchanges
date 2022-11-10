#Step 0
#Creating points, then connect them using portion of circles.

import numpy as np
from matplotlib import pyplot as plt
from matplotlib import patches
from matplotlib import lines
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







        thresholdStart = thresholdEnd-2
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
    """ A road is a 3-uple of three elements. the first one is a line of straight roads. The second one is a list of curvatures that connect those straight roads. The last one corresponds to the list of angle used to define the arc of circles. A road must starts with a straight line, and ends with a straight line.

    Show : Bool. indicate whether of not a plot should be printed """

    #data (input)
    R = [1,1.5,2,2.2, 3] #radius of circles for curvatures
    xs = [1,4,7,2,-4] #absissa for center of circles
    ys = [1,2,-3,-6,-1] #ordinate for center of circles
    points = []

    #for storing tangent points
    tangentPointsL1 = []
    tangentPointsL2 = []

    #for storing arc of circles and tangents
    arc_circles = []
    arc_circles_theta = []
    tangents = []


    #creating points
    for i in range(len(ys)) :
        points.append([xs[i],ys[i]])


    #coordinates of points (center of circles)
    indexForPoints = 0
    while indexForPoints < len(points) - 1:
        xL2 = points[indexForPoints + 1][0]
        xL1 = points[indexForPoints][0]
        yL2 = points[indexForPoints + 1][1]
        yL1 = points[indexForPoints][1]

        #Radius
        R1 = R[indexForPoints]
        R2 = R[indexForPoints + 1]

        #vector L1L2
        xL1L2 = xL2 - xL1
        yL1L2 = yL2 - yL1
        vectorL1L2 = np.array([[xL1L2], [yL1L2]])
        normVectorL1L2 = np.linalg.norm(vectorL1L2)

        #length of the tangent
        d = math.sqrt( normVectorL1L2**2 - (R2 - R1)**2)

        #angle used to build the tangent
        alpha = math.asin((R2 - R1)/normVectorL1L2)

        #rotation matrix based on the angle alpha
        rotationAlpha = np.array([[np.cos(alpha),-np.sin(alpha)], [np.sin(alpha), np.cos(alpha)]])

        #Creation of the intermediate point A
        vectorOL1 = np.array([[xL1],[yL1]])
        vectorOA = vectorOL1 + (d / normVectorL1L2) * np.matmul(rotationAlpha,vectorL1L2)
        xA = vectorOA[0][0]
        yA = vectorOA[1][0]

        #creation of the point B to build tangent (second circle L2)
        vectorOL2 = np.array([[xL2],[yL2]])
        vectorL2A = np.array([[xA - xL2], [yA - yL2]])
        vectorOB = vectorOL2 + (R2 / (R2 - R1)) * vectorL2A
        xB = vectorOB[0][0]
        yB = vectorOB[1][0]
        tangentPointsL2.append([xB,yB])



        #creation of the point C to build tangent (first circle L1)
        vectorOC = vectorOL1 + (R1 / (np.linalg.norm(vectorL2A))) * vectorL2A
        xC = vectorOC[0][0]
        yC = vectorOC[1][0]
        tangentPointsL1.append([xC,yC])


        #creating tangents
        tangents.append(lines.Line2D([xC,xB],[yC,yB]))

        indexForPoints += 1


    #creation of arc of circles
    indexForPoints = 1
    while indexForPoints < len(points) - 1 :

        xc = points[indexForPoints][0]
        yc = points[indexForPoints][1]
        [xL1,yL1] = tangentPointsL1[indexForPoints]
        [xL2,yL2] = tangentPointsL2[indexForPoints-1]
        radius = R[indexForPoints]


        if (xL2-xc) >= 0 :
            theta2 = math.atan((yL2-yc)/(xL2-xc))
        else :
            theta2 = math.atan((yL2-yc)/(xL2-xc)) + math.pi

        if (xL1-xc) >= 0 :
            theta1 = math.atan((yL1-yc)/(xL1-xc))
        else :
            theta1 = math.atan((yL1-yc)/(xL1-xc)) + math.pi

        arc_circle = patches.Arc((xc,yc),2*radius,2*radius,0,theta1*180/math.pi,theta2*180/math.pi, color = "red",linewidth = 3)
        arc_circles.append(arc_circle)
        arc_circles_theta.append([theta1,theta2])
        indexForPoints += 1


    if show :

        #plotting and showing the results
        axis = plt.gca()
        axis.set_aspect('equal', 'box')


        #plotting points
        plt.scatter(xs,ys)

        #plotting circles around points
        i = 0
        while i < len(points) :
            xc = points[i][0]
            yc = points[i][1]
            r = R[i]
            circle = plt.Circle((xc,yc),r,fill=False,alpha = 0.3)
            axis.add_patch(circle)
            i +=1

        #plotting tangent point B on second circle L2
        for pointB in tangentPointsL2 :
            plt.scatter(pointB[0],pointB[1])

        #plotting tangent point C on first circle L1
        for pointC in tangentPointsL1 :
            plt.scatter(pointC[0],pointC[1])


        #plotting the tangent
        for tangent in tangents :
            axis.add_line(tangent)

        #plotting arc of circles
        for arc_circle in arc_circles :
            axis.add_patch(arc_circle)

        #show the plot
        plt.show()


    #returning a road
    return(arc_circles, tangents, arc_circles_theta)



def sampleRoad(road) :


    """ A road is a 3-uple of two elements. the first one is a line of straight roads. The second one is a list of curvatures that connect those straight roads. The last one corresponds to the list of angle used to define the arc of circles.  This function must returns a list of points corresponding to the sampling process"""

    num = 20

    lines = road[1]
    curvatures = road[0]
    thetas = road[2]


    samplesLines = []
    samplesCurvatures = []



    #sampling lines
    indexLine = 0
    while indexLine < len(lines) :
        line = lines[indexLine]
        dataLine = line.get_data()

        xstart = dataLine[0][0]
        xend = dataLine[0][1]
        ystart = dataLine[1][0]
        yend = dataLine[1][1]

        xsampleLine = np.linspace(xstart,xend,num)
        ysampleLine = np.linspace(ystart,yend,num)

        indexSample = 0
        while indexSample < len(xsampleLine) :
            samplesLines.append([xsampleLine[indexSample],ysampleLine[indexSample]])
            indexSample += 1
        indexLine += 1

    #sampling curvatures
    indexCurvature = 0
    while indexCurvature < len(curvatures) :
        curvature = curvatures[indexCurvature]
        radius = (curvature.get_width()) / 2
        (xc,yc) = curvature.get_center()

        theta1 = thetas[indexCurvature][0]
        theta2 = thetas[indexCurvature][1] % (2*math.pi)

        print((theta1*180/math.pi,theta2*180/math.pi))

        angles = np.linspace(min(theta1,theta2),max(theta1,theta2),num)
        for angle in angles :
            samplesCurvatures.append([radius*np.cos(angle)+xc,radius*np.sin(angle)+yc])

        indexCurvature += 1


     #plotting samples for curvatures
    indexSample = 0
    xs = []
    ys = []
    while indexSample < len(samplesCurvatures) :
        xs.append(samplesCurvatures[indexSample][0])
        ys.append(samplesCurvatures[indexSample][1])
        indexSample += 1
    plt.scatter(xs,ys)


     #plotting samples for lines
    indexSample = 0
    xs = []
    ys = []
    while indexSample < len(samplesLines) :
        xs.append(samplesLines[indexSample][0])
        ys.append(samplesLines[indexSample][1])
        indexSample += 1
    plt.scatter(xs,ys)
    plt.show()













